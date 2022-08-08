package com.miaoshaproject.error;

public enum EmBusinessError implements CommonError{
    //有一些通用错误类型如 10001表示参数不合法
    //导致参数不合法的可能性非常多，比如电话号码格式不对、邮箱、身份证等等
    //我们不想对每个导致参数错误的情况单独设计一个错误枚举类型
    //所以设置一个通用的状态码，但是这个状态码所代表的枚举实例中的
    //errMsg可以通过setMsg自行修改
    //于是达到了一个这样的效果
    //电话号码、邮箱、身份证格式不对的错误状态码都是00001，但是他们的
    //错误描述信息可以不同
    //这样就使得不用冗余地创建许多枚举实例，又能准确定位错误信息。

    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOW_ERROR(10002,"未知错误"),

    //2开头为用户信息相关错误定义
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_FAILED(20002,"用户手机或密码错误"),
    USER_NOT_LOGIN(20003,"用户还未登录"),
    //3开头为交易信息错误
    STOCK_NOT_ENOUGH(30001,"库存不足"),
    MQ_SEND_ERROR(30002,"异步更新失败"),
    RATELIMIT(30003,"活动太火爆了")

    ;
    private int errCode;
    private String errMsg;

    // 私有构造，防止外部调用，外部调用会产生 ”例外“的枚举成员
    private EmBusinessError(int errCode,String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
