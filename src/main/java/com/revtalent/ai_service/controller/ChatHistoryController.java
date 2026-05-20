package com.revtalent.ai_service.controller;

import com.revtalent.ai_service.model.mongo.ChatHistory;
import com.revtalent.ai_service.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService service;

    @PostMapping
    public ChatHistory save(@RequestBody ChatHistory chat) {
        return service.save(chat);
    }

    @GetMapping("/{userId}")
    public List<ChatHistory> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }
}
