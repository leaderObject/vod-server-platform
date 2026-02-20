//package com.atfangyi.tingshu.common.thread;
//
////
////import com.atfangyi.tingshu.common.config.zipkin.ZipkinHelper;
////import com.atfangyi.tingshu.common.config.zipkin.ZipkinTaskDecorator;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.*;
//
///**
// * @author: atguigu
// * @create: 2024-05-12 14:54
// */
//@Configuration
//public class ThreadPoolConfig {
//
//    /**
//     * JDK自带线程池（JUC包提供的）
//     * 核心线程=最大线程数 一般应用两类  N 代表CPU核数  初步得出数值，后续采用压测调整合适数量
//     *   CPU密集型：N/N+1
//     *   （Java应用程序一般）IO密集型：2N/2N+1
//     * 阻塞队列长度：200-500之间
//     * @return
//     */
//    @Bean
//    public Executor threadPoolExecutor(){
//        int count = Runtime.getRuntime().availableProcessors();
//        int threadCount = count*2+1;
//        ThreadPoolExecutor threadPoolExecutor
//                = new ThreadPoolExecutor(
//                threadCount,
//                threadCount,
//                0,
//                TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(300),
//                Executors.defaultThreadFactory(),
//                (task, executor)->{
//                    //将拒绝任务再次提交给线程池执行
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    executor.submit(task);
//                }
//        );
//        //默认核心线程是第一次任务提交才创建
//        threadPoolExecutor.prestartCoreThread();
//        return threadPoolExecutor;
//    }
//
//
//    @Autowired
//    private ZipkinHelper zipkinHelper;
//
//    /**
//     * 项目中选择spring线程池
//     * Spring提供线程池threadPoolTaskExecutor
//     *
//     * @return
//     */
//    @Bean
//    public Executor threadPoolTaskExecutor() {
//        int count = Runtime.getRuntime().availableProcessors();
//        int threadCount = count*2+1;
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        // 核心池大小
//        taskExecutor.setCorePoolSize(threadCount);
//        // 最大线程数
//        taskExecutor.setMaxPoolSize(threadCount);
//        // 队列程度
//        taskExecutor.setQueueCapacity(300);
//        // 线程空闲时间
//        taskExecutor.setKeepAliveSeconds(0);
//        // 线程前缀名称
//        taskExecutor.setThreadNamePrefix("sync-tingshu-Executor--");
//        // 该方法用来设置 线程池关闭 的时候 等待 所有任务都完成后，再继续 销毁 其他的 Bean，
//        // 这样这些 异步任务 的 销毁 就会先于 数据库连接池对象 的销毁。
//        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
//        // 任务的等待时间 如果超过这个时间还没有销毁就 强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
//        taskExecutor.setAwaitTerminationSeconds(300);
//        //设置解决zipkin链路追踪不完整装饰器对象
//        taskExecutor.setTaskDecorator(new ZipkinTaskDecorator(zipkinHelper));
//        // 线程不够用时由调用的线程处理该任务
//        taskExecutor.setRejectedExecutionHandler((task, executor)->{
//            //将拒绝任务再次提交给线程池执行
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            executor.submit(task);
//        });
//        return taskExecutor;
//    }
//}
