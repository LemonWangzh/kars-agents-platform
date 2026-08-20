package com.kars.config;

import com.kars.assistant.AiAssistant;
import com.kars.assistant.AiCacheAssistant;
import com.kars.assistant.AiChatPersistenceAssistance;
import com.kars.assistant.AiStockToolAssistant;
import com.kars.listener.BaseListener;
import com.kars.tool.StockHandler;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxImageSize;
import dev.langchain4j.community.model.dashscope.WanxImageStyle;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class LLMConfig {

    private static final Logger log = LoggerFactory.getLogger(LLMConfig.class);



//    @Bean("deepseek")
    public ChatModel chatModelDeepseek(){
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-v4-flash")
                .baseUrl("https://api.deepseek.com").build();
    }


    @Bean("qwen")
    public ChatModel chatModelQwen(){
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("QWEN_API_KEY"))
                .modelName("qwen-long")
                .logRequests(true)
                .logResponses(true)
//                .maxRetries(3)
//                .timeout(Duration.ofSeconds(2))
                .listeners(List.of(new BaseListener()))
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1").build();
    }

    @Bean
    public WanxImageModel wanxImageModel(){
        return WanxImageModel.builder()
                .apiKey(System.getenv("QWEN_API_KEY"))
                .modelName("wanx2.1-t2i-plus")
                .style(WanxImageStyle.WATERCOLOR)
                .size(WanxImageSize.SIZE_1024_1024)
                .build();
    }

//    @Bean
    public AiAssistant aiAssistant(@Qualifier("qwen") ChatModel chatModelQwen) {
        return AiServices.create(AiAssistant.class, chatModelQwen);
    }

    @Bean
    public ChatModelListener first(){
        return new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                ChatModelListener.super.onRequest(requestContext);
                log.info("onRequest(): {}", requestContext.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                ChatModelListener.super.onResponse(responseContext);
                log.info("onResponse(): {}", responseContext.chatResponse());
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                ChatModelListener.super.onError(errorContext);
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };
    }

    @Bean
    public StreamingChatModel streamingChatModel(){
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("QWEN_API_KEY"))
                .modelName("qwen3.8-max")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
    }

    @Bean
    public AiAssistant aiAssistant(StreamingChatModel streamingChatModel){
        return AiServices.create(AiAssistant.class, streamingChatModel);
    }

    @Bean(name = "chatMessageWindowChatMemory")
    public AiCacheAssistant chatMessageWindowChatMemory(StreamingChatModel streamingChatModel){
        return AiServices.builder(AiCacheAssistant.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
                .streamingChatModel(streamingChatModel)
                .build();
    }

    @Bean(name = "chatTokenWindowChatMemory")
    public AiCacheAssistant chatTokenWindowChatMemory(ChatModel chatModel, StreamingChatModel streamingChatModel){
        TokenCountEstimator tokenCountEstimator = new OpenAiTokenCountEstimator("gpt-4");
        return AiServices.builder(AiCacheAssistant.class)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.withMaxTokens(1000, tokenCountEstimator))
                .streamingChatModel(streamingChatModel)
                .build();
    }

    @Bean(name = "chatRedisMessageWindowChatMemory")
    public AiChatPersistenceAssistance chatRedisMessageWindowChatMemory(StreamingChatModel streamingChatModel, RedisChatMemoryStore redisChatMemoryStore){
        ChatMemoryProvider chatMemoryProvider = memoryId ->
                MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)
                        .maxMessages(1000)
                        .build();

        return AiServices.builder(AiChatPersistenceAssistance.class)
                .chatMemoryProvider(chatMemoryProvider)
                .streamingChatModel(streamingChatModel)
                .build();
    }

    @Bean(name = "chatMysqlMessageWindowChatMemory")
    public AiChatPersistenceAssistance chatMysqlMessageWindowChatMemory(StreamingChatModel streamingChatModel, MysqlChatMemoryStore mysqlChatMemoryStore){
        ChatMemoryProvider chatMemoryProvider = memoryId ->
                MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(mysqlChatMemoryStore)
                        .maxMessages(1000)
                        .build();

        return AiServices.builder(AiChatPersistenceAssistance.class)
                .chatMemoryProvider(chatMemoryProvider)
                .streamingChatModel(streamingChatModel)
                .build();
    }


    /**
     * 配置说明参见 {@link <a href="https://docs.langchain4j.info/tutorials/tools"></a>}
     */
    @Bean
    public AiStockToolAssistant tool(StreamingChatModel streamingChatModel, MysqlChatMemoryStore mysqlChatMemoryStore){
        ChatMemoryProvider chatMemoryProvider = memoryId ->
                MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(mysqlChatMemoryStore)
                        .maxMessages(1000)
                        .build();

        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("股票咨询助手")
                .description("根据用户提问的股票信息，分析股票")
                .parameters(JsonObjectSchema.builder()
                        .addStringProperty("stockName", "股票名称")
                        .addStringProperty("stockNum", "股票编号")
                        .addNumberProperty("price", "股票价格")
                        .build())
                .build();

        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            log.info("toolExecutionRequest id = {}", toolExecutionRequest.id());
            log.info("toolExecutionRequest name={}", toolExecutionRequest.name());
            log.info("toolExecutionRequest arguments={}", toolExecutionRequest.arguments());
            return "股票分析成功!";
        };
        return AiServices.builder(AiStockToolAssistant.class)
                .chatMemoryProvider(chatMemoryProvider)
                .streamingChatModel(streamingChatModel)
                .tools(Map.of(toolSpecification, toolExecutor))
                .build();
    }

    @Bean("aiStockToolAssistantPlus")
    public AiStockToolAssistant aiStockToolAssistant(StreamingChatModel streamingChatModel, MysqlChatMemoryStore mysqlChatMemoryStore){
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(mysqlChatMemoryStore)
                .maxMessages(1000)
                .build();

        return AiServices.builder(AiStockToolAssistant.class)
                .chatMemoryProvider(chatMemoryProvider)
                .streamingChatModel(streamingChatModel)
                .tools(new StockHandler()).build();
    }



}
