package dev.notevault.notes;

import java.time.OffsetDateTime;

public record Note(long id, String owner, String title, String body, OffsetDateTime createdAt) {
}
