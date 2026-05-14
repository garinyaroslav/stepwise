package com.github.stepwise.audit;

import java.time.Instant;

public record AuditEntry<T>(
        Long revision,
        Instant timestamp,
        String changedBy,
        String ipAddress,
        T snapshot) {
}
