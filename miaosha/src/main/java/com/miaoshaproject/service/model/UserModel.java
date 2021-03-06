package com.miaoshaproject.service.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserModel {

    private Integer id;
    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotNull(message = "性别不能不填")
    private Byte gender;

    @NotNull(message = "年龄不能不填")
    @Min(value = 0,message = "年龄必须大于0")
    @Max(value = 150,message = "年龄不能大于150岁")
    private Integer age;

    @NotNull(message = "电话不能为空")
    private String telphone;

    private String registerMode;
    private String thridPartyId;

    @NotNull(message = "电话不能为空")
    private String encrptPassword;

}
