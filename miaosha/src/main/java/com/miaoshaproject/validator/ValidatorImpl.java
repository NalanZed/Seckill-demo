package com.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    public ValidationResult validate(Object bean){
        ValidationResult result = new ValidationResult();
        final Set<ConstraintViolation<Object>> constranitViolationSet = validator.validate(bean);
        if(constranitViolationSet.size()>0){
            //有错误
            result.setHasErrors(true);
            constranitViolationSet.forEach(constranitViolation->{
                String errMsg = constranitViolation.getMessage();
                String propertyName = constranitViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return result;
    }

    //在bean初始化完成后会回调该方法
    //
    @Override
    public void afterPropertiesSet() throws Exception {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
