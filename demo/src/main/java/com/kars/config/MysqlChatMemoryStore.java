package com.kars.config;

import com.kars.entity.ChatMemoryRecord;
import com.kars.mapper.ChatMemoryMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MysqlChatMemoryStore implements ChatMemoryStore {

    @Resource
    private ChatMemoryMapper chatMemoryMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String userId = String.valueOf(memoryId);
        ChatMemoryRecord record = chatMemoryMapper.selectByUserId(userId);
        if (record == null || record.getMessages() == null) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(record.getMessages());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String userId = String.valueOf(memoryId);
        String json = ChatMessageSerializer.messagesToJson(messages);

        ChatMemoryRecord existing = chatMemoryMapper.selectByUserId(userId);
        if (existing == null) {
            ChatMemoryRecord record = new ChatMemoryRecord();
            record.setUserId(userId);
            record.setMessages(json);
            record.setDeleted(false);
            chatMemoryMapper.insert(record);
        } else {
            existing.setMessages(json);
            chatMemoryMapper.updateById(existing);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String userId = String.valueOf(memoryId);
        chatMemoryMapper.deleteByUserId(userId);
    }
}
