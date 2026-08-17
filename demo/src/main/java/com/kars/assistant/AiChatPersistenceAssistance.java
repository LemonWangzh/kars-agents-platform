package com.kars.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiChatPersistenceAssistance {

    Flux<String> chat(@MemoryId Long id, @UserMessage String msg);

}
