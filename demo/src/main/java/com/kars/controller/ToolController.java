package com.kars.controller;

import com.kars.assistant.AiStockToolAssistant;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/tool")
public class ToolController {

    @Resource(name = "aiStockToolAssistantPlus")
    private AiStockToolAssistant aiStockToolAssistant;


    @GetMapping("/weather")
    public Flux<String> weather(@RequestParam("prompt") String prompt, @RequestParam("id") Long id) {
        return aiStockToolAssistant.chat(id, prompt);
    }

}
