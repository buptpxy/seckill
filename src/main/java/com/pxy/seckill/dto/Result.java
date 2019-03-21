package com.pxy.seckill.dto;

import com.pxy.seckill.exception.CodeMsg;

/*dto包用来封装service层传给controller层的数据*/
/*传入参数为泛型的类*/
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    private Result(T data) {
        this.code = code;
        this.msg = "success";
        this.data = data;
    }
    private Result(CodeMsg cm){
        if(cm==null){
            return;
        }
        this.code=cm.getCode();
        this.msg=cm.getMsg();
    }
    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
    public T getData(){
        return data;
    }
    //used when succeed
    public static <T> Result<T> success(T data){
        return new Result<T>(data);
    }
    //used when failed
    public static <T> Result<T> error(CodeMsg cm){
        return new Result<T>(cm);
    }
}
