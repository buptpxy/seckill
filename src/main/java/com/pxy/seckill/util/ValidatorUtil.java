package com.pxy.seckill.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    private static final Pattern moblie_pattern = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String src){
        if (StringUtils.isEmpty(src)){
            return false;
        }
        Matcher m = moblie_pattern.matcher(src);
        return m.matches();
    }
}
