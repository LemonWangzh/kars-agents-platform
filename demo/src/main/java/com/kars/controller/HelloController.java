package com.kars.controller;

import com.kars.assistant.AiAssistant;
import com.kars.assistant.AiCacheAssistant;
import com.kars.prompt.StockPrompt;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
public class HelloController {

    @Resource
    @Qualifier("qwen")
    private ChatModel chatModel;

    @Resource
    private WanxImageModel wanxImageModel;

    @Value("classpath:static/images/zycx.png")
    private org.springframework.core.io.Resource resource;

    @Resource
    private AiAssistant aiAssistant;
    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource(name = "chatMessageWindowChatMemory")
    private AiCacheAssistant aiCacheAssistant;




    @PostMapping("/hello")
    @ResponseBody
    private String hello(@RequestParam(value = "question", defaultValue = "你是谁") String question){
//        ChatResponse response = chatModel.chat(UserMessage.from(question));
//        return response.aiMessage().text() + "token 使用： " + response.tokenUsage().totalTokenCount();
        return chatModel.chat(question);
    }

    @PostMapping("/stock")
    private String stock() throws IOException {
        byte[] byteArray = resource.getContentAsByteArray();
        String base64Str = Base64.getEncoder().encodeToString(byteArray);

        UserMessage userMessage = UserMessage.from(
                TextContent.from("从下面图片中分析股票走势"),
                ImageContent.from(base64Str, "image/png")
        );

        ChatResponse response = chatModel.chat(userMessage);

        log.info("response: {}", response);

        return response.aiMessage().text();
    }

    @PostMapping("/create-pic")
    private String createPic(@RequestParam(value = "prompt", defaultValue = "美女") String prompt) {
        Response<Image> generate = wanxImageModel.generate(prompt);
        return generate.content().url().toString();
    }


    @GetMapping(value = "/streamChat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private Flux<String> streamChat(@RequestParam(value = "prompt", defaultValue = "你是谁") String prompt){
        return Flux.create(e-> {
            streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    e.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    e.complete();
                }

                @Override
                public void onError(Throwable error) {
                    e.error(error);
                }
            });
        });
    }

    @GetMapping(value = "/streamChat2")
    private Flux<String> streamChat2(@RequestParam(value = "prompt", defaultValue = "你是谁") String prompt){
        return aiAssistant.chatFlux(prompt);
    }


    @GetMapping(value = "/cacheChat1")
    private String cacheChat1(@RequestParam(value = "id") String id, @RequestParam(value = "prompt", defaultValue = "你是谁") String prompt){
        return aiCacheAssistant.chat(id, prompt);
    }


    @GetMapping(value = "/stockChat")
    private Flux<String> stockChat(@RequestParam(value = "id") String id,
                             @RequestParam(value = "prompt", defaultValue = "你是谁") String prompt,
                             @RequestParam("limit") Integer limit){
        return aiCacheAssistant.stockChat(id, prompt, limit);
    }

    @GetMapping(value = "/stockChat2")
    private Flux<String> stockChat2(@RequestParam(value = "id") String id,
                                   @RequestParam(value = "stock", defaultValue = "北京君正") String stock,
                                   @RequestParam("num") Integer num){
        StockPrompt stockPrompt = new StockPrompt();
        stockPrompt.setName(stock);
        stockPrompt.setNum(num);
        return aiCacheAssistant.stockChat(id, stockPrompt);
    }


    @GetMapping(value = "/stockChat3")
    private String stockChat3(@RequestParam(value = "id") String id,
                                    @RequestParam(value = "stock", defaultValue = "北京君正") String stock,
                                    @RequestParam("num") Integer num){
        PromptTemplate template = PromptTemplate.from("你是一个{{it}}助手，{{question}}");
        Prompt apply = template.apply(Map.of("it", "股票咨询", "question", "帮我看下北京君正的"));
        UserMessage userMessage = apply.toUserMessage();
        return chatModel.chat(userMessage).aiMessage().text();
    }
}
