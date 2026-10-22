package dev.tickethub.catalog;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Instant startsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("billingOrder")
    private List<EventPerformer> lineup = new ArrayList<>();

    @Version
    private Long version;

    protected Event() {
    }

    public Event(String title, Instant startsAt, Venue venue, Category category) {
        this.title = title;
        this.startsAt = startsAt;
        this.venue = venue;
        this.category = category;
    }

    public void addPerformer(Performer p, int billingOrder) {
        lineup.add(new EventPerformer(this, p, billingOrder));
    }

    public void removePerformer(Performer p) {
        lineup.removeIf(ep -> ep.getPerformer().equals(p));
    }

    public void openSale() {
        if (status != EventStatus.DRAFT) throw new IllegalStateException("event is " + status);
        status = EventStatus.ON_SALE;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Instant getStartsAt() { return startsAt; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public Venue getVenue() { return venue; }
    public Category getCategory() { return category; }
    public List<EventPerformer> getLineup() { return lineup; }
    public Long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Event e && id != null && id.equals(e.getId());
    }

    @Override
    public int hashCode() {
        return Event.class.hashCode();
    }
}
