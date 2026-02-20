package com.atfangyi.tingshu.common.ParamAssert;

import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import jodd.util.CollectionUtil;
import jodd.util.StringUtil;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;

public class ServiceAssert {

    public static void ParamAssert(Object object) {
        if (StringUtil.isBlank(object.toString())) throw new GuiguException(ResultCodeEnum.PARAM_ERROR);
    }

    public static void ParamAssert(Collection collection) {
        if (CollectionUtils.isEmpty(collection)) throw new GuiguException(ResultCodeEnum.PARAM_ERROR);

    }

    public static void ObjectAssert(Object object) {
        if (StringUtil.isBlank(object.toString())) throw new GuiguException(ResultCodeEnum.DATA_NULL);
    }

    public static void ObjectAssert(Collection collection) {
        if (CollectionUtils.isEmpty(collection)) throw new GuiguException(ResultCodeEnum.DATA_NULL);

    }





}
