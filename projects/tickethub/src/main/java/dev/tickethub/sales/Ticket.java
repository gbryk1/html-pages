package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import dev.tickethub.inventory.EventSeat;
import jakarta.persistence.*;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private EventSeat eventSeat;

    @Column(nullable = false, length = 160)
    private String holderName;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    private Money price;

    protected Ticket() {
    }

    Ticket(Order order, EventSeat eventSeat, String holderName, Money price) {
        this.order = order;
        this.eventSeat = eventSeat;
        this.holderName = holderName;
        this.price = price;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public EventSeat getEventSeat() { return eventSeat; }
    public String getHolderName() { return holderName; }
    public Money getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Ticket t && id != null && id.equals(t.getId());
    }

    @Override
    public int hashCode() {
        return Ticket.class.hashCode();
    }
}
