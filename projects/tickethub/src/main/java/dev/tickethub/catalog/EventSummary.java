package dev.tickethub.catalog;

import java.time.Instant;

public interface EventSummary {
    Long getId();
    String getTitle();
    Instant getStartsAt();
}
