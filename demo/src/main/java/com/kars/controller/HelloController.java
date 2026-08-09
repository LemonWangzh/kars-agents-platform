package com.kars.controller;

import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@RestController
public class HelloController {

    @Resource
//    @Qualifier("qwen")
    private ChatModel chatModel;

    @Resource
    private WanxImageModel wanxImageModel;

    @Value("classpath:static/images/zycx.png")
    private org.springframework.core.io.Resource resource;


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

}
