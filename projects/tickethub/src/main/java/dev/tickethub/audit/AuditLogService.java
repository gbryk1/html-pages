package dev.tickethub.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository logs;

    public AuditLogService(AuditLogRepository logs) {
        this.logs = logs;
    }

    /** Separate transaction: the entry survives a rollback of the caller. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String details) {
        logs.save(new AuditLog(action, details));
    }
}
