package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";


    //@ExceptionHandler(Exception.class)指明它是一个专门处理异常的处理器
    //Exception.class指明它要捕获并处理的异常类型
    //@ResponseStatus(HttpStatus.OK) 对HttpResponse做了个设置，使其状态码
    //为200 OK，这么做的含义是，请求确实得到响应了，错误不在网络传输中
    // 而是发生在业务层面
    //@ResponseBody 将返回的ex转为json输出，而不是寻找跳转界面


//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public CommonReturnType handlerException(HttpServletRequest request, Exception ex){
//        Map<String,Object> responseData = new HashMap<>();
//        if(ex instanceof BusinessException){
//            BusinessException businessException = (BusinessException)ex;
//            responseData.put("errCode",businessException.getErrCode());
//            responseData.put("errMsg",businessException.getErrMsg());
//        }else {
//            responseData.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrCode());
//            responseData.put("errMsg",EmBusinessError.UNKNOW_ERROR.getErrMsg());
//        }
//        return CommonReturnType.create(responseData,"fail");
//    }
}
