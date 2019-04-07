package com.pxy.seckill.service.impl;

import com.pxy.seckill.dao.SeckillUserDao;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.exception.GlobalException;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.SeckillUserKey;
import com.pxy.seckill.service.SeckillUserService;
import com.pxy.seckill.util.MD5Util;
import com.pxy.seckill.util.UUIDUtil;
import com.pxy.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pxy.seckill.util.MD5Util.inputPassToDbPass;

@Service
public class SeckillUserServiceImpl implements SeckillUserService {
    /**
     * 干啥使？
     */
    public static final String COOKIE_NAME_TOKEN="token";
    @Autowired
    SeckillUserDao seckillUserDao;
    @Autowired
    RedisService redisService;

    @Override
    public SeckillUser getById(long id){
        //取缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById,""+id,SeckillUser.class);
        if (user==null){
            //取数据库
            user = seckillUserDao.getById(id);
            redisService.set(SeckillUserKey.getById,""+id,user);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        //这里的cookie的key都是"token",value是不同的随机值token，所以后面登录的用户会覆盖前面登录的用户的cookie
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    @Override
    public SeckillUser getByToken(HttpServletResponse response,String token){
        if (StringUtils.isEmpty(token)){
            return null;
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user!=null){
            /*为啥redisService中已经有了user，还要set user?延长它的有效期*/
            redisService.set(SeckillUserKey.token,token,user);
            addCookie(response,token,user);
        }
        return user;
    }
    @Override
    public boolean login(HttpServletResponse response, LoginVo loginVo){
        if (loginVo==null){
          throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile=loginVo.getMobile();
        String formPass=loginVo.getPassword();
        //判断手机号是否存在
        SeckillUser user=getById(Long.parseLong(mobile));
        if (user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass=user.getPassword();
        String saltDB=user.getSalt();
        String calcPass= MD5Util.formPassToDBPass(formPass,saltDB);
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //随机生成一个token
        String token = UUIDUtil.uuid();
        redisService.set(SeckillUserKey.token,token,user);
        addCookie(response,token,user);
        return true;
    }

    public boolean updatePassword(String token,long id,String formPass){
        //取user
        SeckillUser user = getById(id);
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库,为啥不直接对取出的user setPassword()?
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        seckillUserDao.update(toBeUpdate);

        //处理缓存，删除redis中(id,user)键值对，修改(token,user)键值对
        // 为啥不是修改(id,user)键值对，因为直接删除比修改时间更短，等下次访问时再放入redis中即可
        //那为啥不对(token,user)也直接删除呢？因为数据库中未存储token，删掉就没有了
        redisService.delete(SeckillUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(SeckillUserKey.token,token,user);
        return true;
    }

    public int insertBatch(){
        List<SeckillUser> list = new ArrayList<SeckillUser>();
        for (int i=4003;i<5000;i++){
            SeckillUser user = new SeckillUser();
            user.setId(15011076722L+i);
            user.setNickname("pxy"+i);
            user.setSalt(i+"dbsalt");
            user.setPassword(inputPassToDbPass("123456",user.getSalt()));
            user.setLastLoginDate(new Date());
            list.add(user);
        }
        return seckillUserDao.insertUserBatch(list);
    }
    public static void main(String[] args){
        ArrayList<SeckillUser> userList = new ArrayList<SeckillUser>();
        for (int i=0;i<1000;i++){
            SeckillUser user = new SeckillUser();
            user.setId(15011076722L+i);
            user.setNickname("pxy"+i);
            user.setSalt(i+"dbsalt");
            user.setPassword(inputPassToDbPass("123456",user.getSalt()));
            user.setLastLoginDate(new Date());
            userList.add(user);
        }
        System.out.println(userList.get(3).getNickname());
    }
}
