package dev.tickethub.catalog;

import jakarta.persistence.*;

@Entity
public class PriceTier {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Sector sector;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    private Money price;

    @Version
    private Long version;

    protected PriceTier() {
    }

    public PriceTier(Event event, Sector sector, Money price) {
        this.event = event;
        this.sector = sector;
        this.price = price;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public Sector getSector() { return sector; }
    public Money getPrice() { return price; }
    public void setPrice(Money price) { this.price = price; }
    public Long getVersion() { return version; }
}
