package com.atfangyi.agent;


import com.atfangyi.agent.advisor.AgentLoggerAdvisor;
import com.atfangyi.agent.tool.*;
import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
@RequestMapping
@CrossOrigin
public class SpringBootAgentApplication {
    private final ChatClient chatClient;

    private final static String SYSTEM_PROMPT = """
            你是一名专业的听书平台运营助手，深谙有声书内容运营、用户增长、会员服务、订单管理等领域。
            请根据我的需求，提供专业、务实且具前瞻性的分析、建议或策略。

            你有以下工具可以使用（优先使用工具获取实时数据）:
            - search_albums: 搜索专辑
            - get_album_ranking: 获取专辑排行榜
            - get_album_detail: 获取专辑详情
            - get_hot_albums: 获取热门专辑
            - get_user_count: 获取用户总数
            - query_user_list: 查询用户列表
            - get_user_detail: 获取用户详情
            - query_order_list: 查询订单列表
            - get_order_detail: 获取订单详情
            - get_pending_orders: 获取待处理订单
            - get_vip_config_list: 获取VIP套餐列表
            - get_category_list: 获取分类列表
            - get_category_albums: 获取某分类下的专辑
            - get_dashboard_stats: 获取仪表盘统计数据
            - get_today_stats: 获取今日数据统计

            当用户询问具体数据时（如用户数、订单数、专辑列表等），请优先使用相应工具获取实时数据。
            只有在用户询问的知识超出工具范围时（如平台规则、运营策略建议），才结合上下文和自己的知识回答。
            """;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAgentApplication.class, args);
    }

    public SpringBootAgentApplication(ChatModel DashScopeChatModel, VectorStore vectorStore,
                                       SearchTool searchTool,
                                       UserTool userTool,
                                       OrderTool orderTool,
                                       VipAndCategoryTool vipAndCategoryTool,
                                       StatsTool statsTool) {
        // 构建 DocumentRetriever (RAG 辅助)
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .build();

        // 构建 ChatClient，注册所有 Tools 和 Advisors
        chatClient = ChatClient.builder(DashScopeChatModel)
                .defaultTools(searchTool, userTool, orderTool, vipAndCategoryTool, statsTool)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new AgentLoggerAdvisor(),
                        new DocumentRetrievalAdvisor(retriever)
                )
                .build();
    }

    private static final String CHAT_ID = "1";

    @GetMapping(value = "/agent/response", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI对话响应")
    public Flux<ServerSentEvent<String>> doChatWithAgentApp(String message) {
        return chatClient.prompt(SYSTEM_PROMPT)
                .user(message)
                .advisors(spec -> spec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, CHAT_ID)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 200))
                .stream()
                .content()
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }
}
