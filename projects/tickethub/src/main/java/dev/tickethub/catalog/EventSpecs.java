package dev.tickethub.catalog;

import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;

public final class EventSpecs {

    private EventSpecs() {
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Event> inCity(String city) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("venue").get("address").get("city")), city.toLowerCase());
    }

    public static Specification<Event> inCategory(String code) {
        return (root, query, cb) -> cb.equal(root.get("category").get("code"), code);
    }

    public static Specification<Event> startsBetween(Instant from, Instant to) {
        return (root, query, cb) -> cb.between(root.get("startsAt"), from, to);
    }

    public static Specification<Event> titleContains(String text) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + text.toLowerCase() + "%");
    }
}
