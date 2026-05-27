package com.revtalent.ai_service.controller;

import com.revtalent.ai_service.model.mongo.ChatHistory;
import com.revtalent.ai_service.service.ChatHistoryService;
import com.revtalent.ai_service.util.SecurityUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class ChatHistoryController {

    private final ChatHistoryService service;
    private final SecurityUserContext securityUserContext;

    @PostMapping
    public ChatHistory save(@RequestBody ChatHistory chat) {
        Long currentUserId = securityUserContext.getCurrentUserId();
        if (chat.getUserId() != null && !chat.getUserId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot save chat history for another user");
        }
        chat.setUserId(currentUserId);
        return service.save(chat);
    }

    @GetMapping("/{userId}")
    public List<ChatHistory> getByUser(@PathVariable Long userId) {
        Long currentUserId = securityUserContext.getCurrentUserId();
        if (!userId.equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        return service.getByUser(userId);
    }
}
