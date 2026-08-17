package com.kars.assistant;


import com.kars.prompt.StockPrompt;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import reactor.core.publisher.Flux;

public interface AiCacheAssistant {

    String chat(@MemoryId String userId, @UserMessage String prompt);


    @SystemMessage("你是一位专业的股票分析师，只回答关于股票相关的问题。" +
            "输出限制：对于其他领域的问题禁止回答，直接返回'抱歉，我只能回答股票相关问题'")
    @UserMessage("请回答下面股票问题：{{question}}，字数控制在{{limit}}以内")
    Flux<String> stockChat(@MemoryId String userId, @V("question") String question, @V("limit") int limit);


    @SystemMessage("你是一位专业的股票分析师，只回答关于股票相关的问题。" +
            "输出限制：对于其他领域的问题禁止回答，直接返回'抱歉，我只能回答股票相关问题'")
    Flux<String> stockChat(@MemoryId String userId,@UserMessage StockPrompt stockPrompt);
}
