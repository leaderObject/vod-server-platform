package com.atfangyi.tingshu.search.client.Impl;

/*
 * @Author:  方毅
 * @date:  2025/11/8 17:13
 */

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.search.client.SearchFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchDegradeFeignClientImpl implements SearchFeignClient {
    @Override
    public Result updateLatelyAlbumRanking() {
        log.error("[检索模块远程调用失败执行熔断降级{}]", "updateLatelyAlbumRanking");
        return null;
    }
}
