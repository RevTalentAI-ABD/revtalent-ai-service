package com.revtalent.ai_service.repository;

import com.revtalent.ai_service.model.AIDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIDocumentRepository extends JpaRepository<AIDocument, Long> {
    List<AIDocument> findByIncludedTrue();
    List<AIDocument> findByUserId(Long userId);
    List<AIDocument> findByUserIdAndIncludedTrue(Long userId);
    java.util.Optional<AIDocument> findByIdAndUserId(Long id, Long userId);
}
