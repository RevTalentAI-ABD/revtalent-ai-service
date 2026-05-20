package com.revtalent.ai_service.service;

import com.revtalent.ai_service.model.mongo.ChatHistory;
import com.revtalent.ai_service.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository repository;

    public ChatHistory save(ChatHistory chat) {
        return repository.save(chat);
    }

    public List<ChatHistory> getByUser(Long userId) {
        return repository.findByUserId(userId);
    }
}
