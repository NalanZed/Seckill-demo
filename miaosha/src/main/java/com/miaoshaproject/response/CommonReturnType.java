package com.miaoshaproject.response;

public class CommonReturnType {
    //表明请求处理的完成状态“success” 或者 “fail”
    private String status;
    //得到的数据信息
    // 如果请求状态为 success,则返回前端需要的json数据
    // 若status为fail，data内返回通用的错误码格式
    private Object data;

    // 如果不带有status信息。默认请求状态为success
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setData(result);
        type.setStatus(status);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
