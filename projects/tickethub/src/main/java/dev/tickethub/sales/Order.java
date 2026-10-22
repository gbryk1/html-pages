package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import dev.tickethub.inventory.EventSeat;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Audited
public class Order extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    private Money total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<Payment> payments = new ArrayList<>();

    @Version
    private Long version;

    protected Order() {
    }

    public Order(Customer customer, String currency) {
        this.customer = customer;
        this.total = new Money(java.math.BigDecimal.ZERO, currency);
    }

    public Ticket addTicket(EventSeat seat, String holderName, Money price) {
        Ticket t = new Ticket(this, seat, holderName, price);
        tickets.add(t);
        total = total.plus(price);
        return t;
    }

    public void removeTicket(Ticket t) {
        if (tickets.remove(t)) {
            total = new Money(total.amount().subtract(t.getPrice().amount()), total.currency());
        }
    }

    public void addPayment(Payment p) {
        p.setOrder(this);
        payments.add(p);
    }

    public void markPaid() {
        if (status != OrderStatus.NEW) throw new IllegalStateException("order is " + status);
        status = OrderStatus.PAID;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Money getTotal() { return total; }
    public List<Ticket> getTickets() { return tickets; }
    public List<Payment> getPayments() { return payments; }
    public Long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Order other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Order.class.hashCode();
    }
}
