/*
 * @Author:  方毅
 * @date:  2025/10/25 14:14
 */

import com.atfangyi.tingshu.ServiceSearchApplication;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ServiceSearchApplication.class)
public class TestDemo {

    @Autowired
    private SearchService searchService;

    @Autowired
    private RedissonClient redissonClient;


    @Test
    public void show() {

        for (int i = 1; i <= 1700; i++) {
            RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
            bloomFilter.add(Long.valueOf(i));
        }

    }

}
