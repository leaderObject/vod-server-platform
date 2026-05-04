package com.atfangyi.agent.advisor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

@Slf4j
@SuppressWarnings({"all"})
public class AgentLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        observeAfter(advisedResponse);
        return advisedResponse;
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        log.debug("AI请求: {}", advisedRequest.userText());
        return advisedRequest;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        log.debug("AI响应完成: {}",
            truncate(advisedResponse.response().getResult().getOutput().getText(), 200));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        log.debug("AI流式请求开始: {}", truncate(advisedRequest.userText(), 100));
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return advisedResponses
                .doOnComplete(() -> log.debug("AI流式响应完成"))
                .doOnError(e -> log.debug("AI流式响应异常: {}", e.getMessage()));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }
}
