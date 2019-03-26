package com.pxy.seckill.exception;

/*各个模块的分类异常*/
public class CodeMsg {
    private int code;
    private String msg;
    private CodeMsg(int code,String msg){
        this.code=code;
        this.msg=msg;
    }
    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }

    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }

    @Override
    public String toString() {
        return "CodeMsg [code=" + code + ", msg=" + msg + "]";
    }


    //common exception
    public static CodeMsg SUCCESS = new CodeMsg(0,"success");
    public static CodeMsg SERVER_ERROR=new CodeMsg(500100,"server exception");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");
    //login module 5002xx
    public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "登录密码不能为空");
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");

    //goods module 5003xx

    //orders module 5004xx

    //seckill module 5005xx
    public static CodeMsg SEC_KILL_OVER = new CodeMsg(500500, "商品已经秒杀完毕");
    public static CodeMsg REPEATE_SEC_KILL = new CodeMsg(500501, "不能重复秒杀");

}
