package com.atfangyi.tingshu.search.repository;

import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/*
 * @Author:  方毅
 * @date:  2025/10/25 8:09
 */
public interface AlbumInfoIndexRepository  extends ElasticsearchRepository<AlbumInfoIndex,Long> {


}
