package com.kars.config;

import com.kars.assistant.AiAssistant;
import com.kars.listener.BaseListener;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxImageSize;
import dev.langchain4j.community.model.dashscope.WanxImageStyle;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

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
//                .modelName("qwen3.8-max")
                .modelName("z-image-turbo")
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

}
