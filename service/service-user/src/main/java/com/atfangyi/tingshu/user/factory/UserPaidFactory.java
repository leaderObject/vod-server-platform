package com.atfangyi.tingshu.user.factory;

import com.atfangyi.tingshu.user.strategy.UserPaidStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/*
 * @Author:  方毅
 * @date:  2025/11/5 21:33
 */
@Component
public class UserPaidFactory {

    @Autowired
    private Map<String, UserPaidStrategy> userPaidStrategyMap;

    public   UserPaidStrategy getUserPaidStrategy(String itemType) {
        return userPaidStrategyMap.get(itemType);
    }

}
