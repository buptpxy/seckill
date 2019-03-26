package com.pxy.seckill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

public class WebConfig  implements WebMvcConfigurer {
    @Autowired
    UserArgumentResolver userArgumentResolver;

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers){
        argumentResolvers.add(userArgumentResolver);
    }

}
