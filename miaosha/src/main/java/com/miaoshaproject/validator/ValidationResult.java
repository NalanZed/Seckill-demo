package com.miaoshaproject.validator;



import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {

    //校验结果
    private boolean hasErrors = false;
    private Map<String,String> errorMsgMap = new HashMap<>();

    // 格式化错误信息
    public String getErrorMsg(){
       return StringUtils.join(errorMsgMap.values().toArray(),",");
    }


    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrorMsgMap() {
        return errorMsgMap;
    }

    public void setErrorMsgMap(Map<String, String> errorMsgMap) {
        this.errorMsgMap = errorMsgMap;
    }

}
