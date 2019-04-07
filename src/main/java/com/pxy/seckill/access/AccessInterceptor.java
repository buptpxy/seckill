package com.pxy.seckill.access;

import com.alibaba.fastjson.JSON;
import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.redis.AccessKey;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * 为定义的注解创建一个拦截器
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    SeckillUserServiceImpl userService;
    @Autowired
    RedisService redisService;

    private String getCookieValue(HttpServletRequest request,String cookieName){
        Cookie[] cookies = request.getCookies();
        if (cookies==null || cookies.length<=0){
            return null;
        }
        for (Cookie cookie:cookies){
            if(cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }

    private SeckillUser getUser(HttpServletRequest request,HttpServletResponse response){
        String paramToken = request.getParameter(SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response,token);
    }

    private void render(HttpServletResponse response,CodeMsg cm)throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));//将相应的内容写入response中。
        out.flush();//将缓存的数据刷入永久存储(文件)中。
        out.close();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler)
        throws Exception{
        if (handler instanceof HandlerMethod){
            SeckillUser user = getUser(request,response);
            UserContext.setUser(user);//此user就是线程私有的对象了
            HandlerMethod hm = (HandlerMethod)handler;
            //通过反射获取到AccessLimit注解的参数
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit==null){
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();//默认为true
            //key为AccessLimit注解的方法的RequestMapping("uri")里面的URI
            String key = request.getRequestURI();
            if (needLogin){
                if (user==null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key+="_"+user.getId();
            }
            //redis中存着此用户的登录次数，有效期为seconds
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak,key,Integer.class);
            if (count == null){
                redisService.set(ak,key,1);
            }else if (count<maxCount){
                redisService.incr(ak,key);
            }else {
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }
}
