package com.atfangyi.tingshu.search.repository;

import com.atfangyi.tingshu.model.search.SuggestIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/*
 * @Author:  方毅
 * @date:  2025/10/27 14:21
 */public interface SuggestionIndexRepository  extends ElasticsearchRepository<SuggestIndex,String> {
}
