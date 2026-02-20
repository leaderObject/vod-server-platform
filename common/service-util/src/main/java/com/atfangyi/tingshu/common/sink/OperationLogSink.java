package com.atfangyi.tingshu.common.sink;

import com.atfangyi.tingshu.common.entity.Log;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
public interface OperationLogSink {

    void publish(Log log);


}
