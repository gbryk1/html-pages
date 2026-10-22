package dev.tickethub.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EventRepository events;

    public EventController(EventRepository events) {
        this.events = events;
    }

    @GetMapping("/events")
    public Page<EventCard> list(@RequestParam(defaultValue = "0") int page) {
        return events.findCards(EventStatus.ON_SALE, PageRequest.of(page, 20, Sort.by("startsAt", "id")));
    }
}
