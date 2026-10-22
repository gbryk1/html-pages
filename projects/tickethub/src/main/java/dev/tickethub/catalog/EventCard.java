package dev.tickethub.catalog;

import java.time.Instant;

public record EventCard(Long id, String title, Instant startsAt, String venueName, String city) {
}
