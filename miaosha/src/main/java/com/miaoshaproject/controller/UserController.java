package com.miaoshaproject.controller;


import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@RestController("user")
@RequestMapping("/user")
// 跨域问题解决
// 单纯地使用CrossOrigin解决跨域问题，会导致ajax请求之间session无法共享
// 这也就是为什么在进行注册服务时，
// 会发现从session中取到的otpCode是null而不是在getotp服务中存储的值
// 在这里指定allowCredentials= true 允许跨域授信
@CrossOrigin(originPatterns = "*",allowCredentials="true",allowedHeaders = "*",methods = {})
//@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {}, allowCredentials = "true")
public class UserController extends BaseController{



    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //根据给出的id返回用户对象
    @ResponseBody
    @RequestMapping("/get")
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);
        //如果用户信息不存在
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        // 将领域模型用户对象转换为UI层使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        //产生通用对象并返回
        CommonReturnType result = CommonReturnType.create(userVO);
        return result;
    }

    @RequestMapping( value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        // 1.按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String optCode = String.valueOf(randomInt);
        // 2.将OTP验证码同对应用户的手机号关联
        //使用httpseesion的方式绑定他的手机号与OTPCODE
        HttpSession session = this.httpServletRequest.getSession();
        session.setAttribute(telphone,optCode);
        // 3.将OTP验证码通过信息通道发送给用户（省略）
        System.out.println("telphone:" + telphone +"  otpcode" + optCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="gender")Byte gender,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证手机号和otpCode是否相符，可以确保本人在使用手机
        //从session中获取之前存入的optCode
        HttpSession session = this.httpServletRequest.getSession();
        String inSessionOtpCode = (String)session.getAttribute(telphone);
        // alibaba中StringUtil会自动判空
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码错误");
        }
        // 进入注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setTelphone(telphone);
        userModel.setEncrptPassword(this.EncodeByMD5(password));
        userModel.setRegisterMode("byphone");
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"账号密码不能为空");
        }

        UserModel userModel = userService.validateLogin(telphone, EncodeByMD5(password));
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAILED);
        }
        // 验证通过，加入session
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }



    public String EncodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(str.getBytes("utf-8"));
        byte[] bytes = Base64.encodeBase64(digest);
        String newsrt = new String(bytes);
        return newsrt;
    }

    public UserVO convertFromModel(UserModel userModel){
        UserVO userVO = new UserVO();
        if(userModel!=null){
            BeanUtils.copyProperties(userModel,userVO);
        }
        return userVO;
    }
}
