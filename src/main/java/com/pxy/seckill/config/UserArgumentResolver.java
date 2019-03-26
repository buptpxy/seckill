package com.pxy.seckill.config;

import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    SeckillUserServiceImpl userService;

    private String getCookieValue(HttpServletRequest request,String cookieName){
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }

    public boolean supportsParameter(MethodParameter parameter){
        Class<?> clazz = parameter.getParameterType();
        return clazz== SeckillUser.class;
    }

    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory){
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        String paramToken = request.getParameter(SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken) ){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response,token);
    }

}
