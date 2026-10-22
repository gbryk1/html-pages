package dev.tickethub;

import dev.tickethub.audit.AuditLog;
import dev.tickethub.audit.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class Inner {
    private final AuditLogRepository logs;
    Inner(AuditLogRepository logs) { this.logs = logs; }
    @Transactional
    public void failing() {
        logs.save(new AuditLog("INNER", "x"));
        throw new IllegalStateException("inner failed");
    }
}

@Service
public class Propagation {
    private final Inner inner;
    Propagation(Inner inner) { this.inner = inner; }
    @Transactional
    public void outer() {
        try { inner.failing(); } catch (IllegalStateException e) { System.out.println("PROP caught"); }
    }
}
