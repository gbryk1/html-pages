package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "method", length = 20)
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @Embedded
    private Money amount;

    protected Payment() {
    }

    protected Payment(Money amount) {
        this.amount = amount;
    }

    void setOrder(Order order) { this.order = order; }

    public abstract String describe();

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Money getAmount() { return amount; }
}
