package com.kars.assistant;

import dev.langchain4j.service.SystemMessage;

//@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "qwen")
public interface AiAssistant {

    @SystemMessage("You are a polite assistant")
    String chat(String userMessage);

}
