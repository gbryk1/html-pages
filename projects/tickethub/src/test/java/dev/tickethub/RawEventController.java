package dev.tickethub;

import dev.tickethub.catalog.Event;
import dev.tickethub.catalog.EventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RawEventController {
    private final EventRepository events;
    public RawEventController(EventRepository events) { this.events = events; }

    @GetMapping("/raw/events/{id}")
    public Event raw(@PathVariable Long id) {
        return events.findById(id).orElseThrow();
    }
}
