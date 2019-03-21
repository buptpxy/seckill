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
    //common exception
    public static CodeMsg SUCCESS = new CodeMsg(0,"success");
    public static CodeMsg SERVER_ERROR=new CodeMsg(500100,"server exception");

    //login module 5002xx

    //goods module 5003xx

    //orders module 5004xx

    //seckill module 5005xx
}
