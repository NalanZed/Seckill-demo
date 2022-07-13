package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.StyledEditorKit;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns = "*",allowCredentials="true",allowedHeaders = "*",methods = {})
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    private CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                         @RequestParam(name = "amount")Integer amount,
                                         @RequestParam(name= "promoId",required = false)Integer promoId) throws BusinessException {
        // 获取用户的登录信息
        Boolean is_login = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(is_login == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        // 从登录session中直接获得userModel信息
        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,amount,promoId);
       return CommonReturnType.create(null);
    }
}
