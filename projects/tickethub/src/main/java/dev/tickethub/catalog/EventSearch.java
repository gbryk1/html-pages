package dev.tickethub.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

import static dev.tickethub.catalog.EventSpecs.*;

@Service
public class EventSearch {

    public record Criteria(String city, String category, Instant from, Instant to, String text) {
    }

    private final EventRepository events;

    public EventSearch(EventRepository events) {
        this.events = events;
    }

    @Transactional(readOnly = true)
    public Page<Event> search(Criteria c, Pageable page) {
        Specification<Event> spec = hasStatus(EventStatus.ON_SALE);
        if (c.city() != null) spec = spec.and(inCity(c.city()));
        if (c.category() != null) spec = spec.and(inCategory(c.category()));
        if (c.from() != null && c.to() != null) spec = spec.and(startsBetween(c.from(), c.to()));
        if (c.text() != null) spec = spec.and(titleContains(c.text()));
        return events.findAll(spec, page);
    }
}
