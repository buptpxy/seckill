package com.pxy.seckill.vo;

import com.pxy.seckill.validator.IsMobile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * vo包一般用来封装从entity中抽象出来的对象，传给Service层
 */
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min=32)
    private String password;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
    }
}
