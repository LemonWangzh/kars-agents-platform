package com.kars.controller;

import com.kars.assistant.AiCacheAssistant;
import com.kars.assistant.AiChatPersistenceAssistance;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class PersistenceController {

    @Resource(name = "chatRedisMessageWindowChatMemory")
    private AiChatPersistenceAssistance aiChatPersistenceAssistance;

    @Resource(name = "chatMysqlMessageWindowChatMemory")
    private AiChatPersistenceAssistance aiMysqlCHat;

    @GetMapping(value = "/redis-chat")
    private Flux<String> stockChat(@RequestParam(value = "id") Long id,
                                   @RequestParam(value = "prompt", defaultValue = "你是谁") String prompt,
                                   @RequestParam("limit") Integer limit){
        return aiChatPersistenceAssistance.chat(id, prompt);
    }

    @GetMapping(value = "/mysql-chat")
    private Flux<String> stockMysqlChat(@RequestParam(value = "id") Long id,
                                   @RequestParam(value = "prompt", defaultValue = "你是谁") String prompt,
                                   @RequestParam("limit") Integer limit){
        return aiMysqlCHat.chat(id, prompt);
    }

}
