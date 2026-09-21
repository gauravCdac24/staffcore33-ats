package com.staffcore33.ats.audit;

import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, String details) {
        Long userId = null;
        try {
            userId = SecurityUtils.currentUserId();
        } catch (Exception ignored) {
        }
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .createdAt(java.time.Instant.now())
                .build());
    }
}
