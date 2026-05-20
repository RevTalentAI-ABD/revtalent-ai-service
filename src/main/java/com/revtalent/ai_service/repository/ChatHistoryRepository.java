package com.revtalent.ai_service.repository;

import com.revtalent.ai_service.model.mongo.ChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {

    List<ChatHistory> findByUserId(Long userId);
}
