package dev.tickethub.sales;

import dev.tickethub.audit.AuditLogService;
import dev.tickethub.catalog.Money;
import dev.tickethub.catalog.PriceTier;
import dev.tickethub.catalog.PriceTierRepository;
import dev.tickethub.inventory.EventSeat;
import dev.tickethub.inventory.EventSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CheckoutService {

    public record Request(String email, Long eventId, int seats, String method, String paymentRef) {
    }

    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final EventSeatRepository eventSeats;
    private final PriceTierRepository tiers;
    private final PaymentGateway gateway;
    private final AuditLogService audit;

    public CheckoutService(CustomerRepository customers, OrderRepository orders, EventSeatRepository eventSeats,
                           PriceTierRepository tiers, PaymentGateway gateway, AuditLogService audit) {
        this.customers = customers;
        this.orders = orders;
        this.eventSeats = eventSeats;
        this.tiers = tiers;
        this.gateway = gateway;
        this.audit = audit;
    }

    @Transactional(rollbackFor = PaymentDeclinedException.class)
    public Long placeOrder(Request r) throws PaymentDeclinedException {
        audit.record("CHECKOUT_ATTEMPT", r.email() + " event=" + r.eventId());
        Customer customer = customers.findByEmail(r.email()).orElseThrow();
        List<EventSeat> seats = eventSeats.grabAvailable(r.eventId(), r.seats());
        if (seats.size() < r.seats()) throw new IllegalStateException("not enough seats");

        Order order = new Order(customer, "PLN");
        for (EventSeat es : seats) {
            PriceTier tier = tiers.findByEventIdAndSectorId(r.eventId(), es.getSeat().getSector().getId()).orElseThrow();
            es.sell();
            order.addTicket(es, customer.getFullName(), tier.getPrice());
        }
        Money total = order.getTotal();
        order.addPayment(gateway.charge(r.method(), r.paymentRef(), total));
        order.markPaid();
        return orders.save(order).getId();
    }
}
