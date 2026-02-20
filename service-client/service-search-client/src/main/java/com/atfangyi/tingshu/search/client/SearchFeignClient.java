package com.atfangyi.tingshu.search.client;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.search.client.Impl.SearchDegradeFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * @Author:  方毅
 * @date:  2025/11/8 17:11
 */
@FeignClient(value = "service-search", url = "api/search", fallback = SearchDegradeFeignClientImpl.class)
public interface SearchFeignClient {

    @GetMapping("/albumInfo/updateLatelyAlbumRanking")
    Result updateLatelyAlbumRanking();

}
