package com.kars.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Resource
    private RedisTemplate<String, String> redisTemplate;


    private final static String CACHE_MEMORY_KEY = "CACHE_MEMORY_KEY:";

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return ChatMessageDeserializer.messagesFromJson(Optional.ofNullable(redisTemplate.opsForValue().
                get(CACHE_MEMORY_KEY + memoryId)).map(Object::toString).orElse(null));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Optional.ofNullable(memoryId).ifPresent(id-> redisTemplate.opsForValue().set(CACHE_MEMORY_KEY + id, ChatMessageSerializer.messagesToJson(messages)));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Optional.ofNullable(memoryId).ifPresent(id -> redisTemplate.delete(CACHE_MEMORY_KEY + id));
    }
}
