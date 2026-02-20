package com.atfangyi.agent;


import com.atfangyi.agent.advisor.AgentLoggerAdvisor;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
@RequestMapping
@CrossOrigin
public class SpringBootAgentApplication {
    private final ChatClient chatClient;

    private final static String PROMPT = "你是一名深谙欧洲足球五大联赛（英超、西甲、意甲、德甲、法甲）的足球经理人，请根据我的需求，提供专业、务实且具前瞻性的分析、建议或策略";


    public static void main(String[] args) {
        SpringApplication.run(SpringBootAgentApplication.class, args);
    }

    public SpringBootAgentApplication(ChatModel DashScopeChatModel) {
        chatClient = ChatClient.builder(DashScopeChatModel).defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                ,new AgentLoggerAdvisor()).build();
    }

    private static final String CHAT_ID = "1";

    @GetMapping("/agent/response")
    @Operation(summary = "响应内内容")
    public Flux<ServerSentEvent<String>> doChatWithAgentApp(String message) {
        return chatClient.prompt(PROMPT).user(message).advisors(spec -> spec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, CHAT_ID).param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 200)).stream().content().map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());

    }
}