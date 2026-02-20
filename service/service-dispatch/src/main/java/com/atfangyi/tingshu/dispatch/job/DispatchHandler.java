package com.atfangyi.tingshu.dispatch.job;

import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.search.client.SearchFeignClient;
import com.atfangyi.tingshu.user.client.UserFeignClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;

@Slf4j
@Component
public class DispatchHandler {


    @Autowired
    private SearchFeignClient searchFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @XxlJob("firstJobHandler")
    public void firstJobHandler() {
        log.info("xxl-job项目集成测试{}", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
    }


    @XxlJob("HotRanking")
    public void HotRanking() {
        searchFeignClient.updateLatelyAlbumRanking();
        XxlJobHelper.log("热度排行榜更新完成->{}", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
    }


    @XxlJob("UserVipStatus")
    public void UserVipStatus() {
        userFeignClient.queryUserVipStatus();
        XxlJobHelper.log("更新用户vip状态成功", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
    }

    @XxlJob("TestJob")
    public  void  test(){
             log.info("测试xxl job");
    }

}