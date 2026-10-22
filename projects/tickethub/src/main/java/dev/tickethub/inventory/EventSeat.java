package dev.tickethub.inventory;

import dev.tickethub.catalog.Event;
import dev.tickethub.catalog.Seat;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private Instant heldUntil;

    @Version
    private long version;

    protected EventSeat() {
    }

    public EventSeat(Event event, Seat seat) {
        this.event = event;
        this.seat = seat;
    }

    public void sell() {
        if (status == SeatStatus.SOLD) throw new IllegalStateException("seat already sold: " + id);
        status = SeatStatus.SOLD;
        heldUntil = null;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public Seat getSeat() { return seat; }
    public SeatStatus getStatus() { return status; }
    public long getVersion() { return version; }
}
