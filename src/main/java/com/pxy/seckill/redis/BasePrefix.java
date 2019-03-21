package com.pxy.seckill.redis;

public abstract class BasePrefix implements KeyPrefix {
    /**
     * 键的过期时间
     */
    private int expireSeconds;
    /**
     * 键的前缀
     */
    private String prefix;

    public int expireSeconds(){
        return expireSeconds;
    }

    /**
     * getClass()是获取类的类模板实例对象，通过反射的机制获取。
     * Class.getSimpleName()方法是获取源代码中给出的‘底层类’简称
     * 而Class.getName()以String的形式，返回Class对象的‘实体’名称，全限定名？
     * @return
     */
    public String getPrefix(){
        String className=getClass().getSimpleName();
        return className+":"+prefix;
    }

    public BasePrefix(int expireSeconds,String prefix){
        this.expireSeconds=expireSeconds;
        this.prefix=prefix;
    }

    /**
     * 构造时未设置过期时间则默认过期时间为0，立马过期？
     * @param prefix
     */
    public BasePrefix(String prefix){
        this(0,prefix);
    }
}
