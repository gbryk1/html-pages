package dev.tickethub.catalog;

import jakarta.persistence.*;

@Entity
public class EventPerformer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Performer performer;

    private int billingOrder;

    protected EventPerformer() {
    }

    EventPerformer(Event event, Performer performer, int billingOrder) {
        this.event = event;
        this.performer = performer;
        this.billingOrder = billingOrder;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public Performer getPerformer() { return performer; }
    public int getBillingOrder() { return billingOrder; }
}
