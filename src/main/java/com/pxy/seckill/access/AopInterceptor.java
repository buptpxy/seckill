package com.pxy.seckill.access;

import com.alibaba.fastjson.JSON;
import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.redis.AccessKey;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Method;


@Aspect //声明一个切面
@Component //让这个切面成为spring管理的Bean
public class AopInterceptor {
    @Autowired
    SeckillUserServiceImpl userService;
    @Autowired
    RedisService redisService;

    //声明切点
    @Pointcut("@annotation(com.pxy.seckill.access.AccessLimit)")
    public void beforePointCut(){}

    //声明一个建言，并使用@PointCut定义的切点作为拦截规则
    @Before("beforePointCut()")
    public void before(JoinPoint joinPoint) throws Exception {
        //获取HttpRequest
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        if (request==null){
            return;
        }
        //获得注解
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method ==null){
            return;
        }
        AccessLimit accessLimit = method.getAnnotation(AccessLimit.class);
        if (accessLimit==null){
            return ;
        }
        SeckillUser user = getUser(request,response);
        UserContext.setUser(user);//此user就是线程私有的对象了
        int seconds = accessLimit.seconds();
        int maxCount = accessLimit.maxCount();
        boolean needLogin = accessLimit.needLogin();//默认为true
        //key为AccessLimit注解的方法的RequestMapping("uri")里面的URI
        String key = request.getRequestURI();
        if (needLogin){
            if (user==null){
                render(response,CodeMsg.SESSION_ERROR);
                return ;
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
            return ;
        }
    }




    private String getCookieValue(HttpServletRequest request, String cookieName){
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

    private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response,token);
    }

    private void render(HttpServletResponse response, CodeMsg cm)throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));//将相应的内容写入response中。
        out.flush();//将缓存的数据刷入永久存储(文件)中。
        out.close();
    }



}
