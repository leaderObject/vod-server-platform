package com.atfangyi.agent.advisor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

@Slf4j
@SuppressWarnings({"all"})
public class AgentLoggerAdvisor  implements CallAroundAdvisor, StreamAroundAdvisor {
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        observeAfter(advisedResponse);
        return advisedResponse;
    }
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
         log.debug("request--> {}",advisedRequest.userText());
        return  advisedRequest;
    }
    private void observeAfter(AdvisedResponse advisedResponse) {
        log.debug("response--> {}",advisedResponse.response().getResult().getOutput().getText());
    }
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
    @Override
    public String getName() {
        return   this.getClass().getSimpleName();
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
