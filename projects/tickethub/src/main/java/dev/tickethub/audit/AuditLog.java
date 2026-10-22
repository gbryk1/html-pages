package dev.tickethub.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Instant at = Instant.now();

    @Column(nullable = false, length = 60)
    private String action;

    @Column(length = 500)
    private String details;

    protected AuditLog() {
    }

    public AuditLog(String action, String details) {
        this.action = action;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}
