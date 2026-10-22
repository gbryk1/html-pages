package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import dev.tickethub.catalog.PriceTier;
import dev.tickethub.catalog.PriceTierRepository;
import dev.tickethub.catalog.SeatRepository;
import dev.tickethub.inventory.EventSeat;
import dev.tickethub.inventory.EventSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FastCheckoutService {

    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final EventSeatRepository eventSeats;
    private final SeatRepository seats;
    private final PriceTierRepository tiers;
    private final PaymentGateway gateway;

    public FastCheckoutService(CustomerRepository customers, OrderRepository orders, EventSeatRepository eventSeats,
                               SeatRepository seats, PriceTierRepository tiers, PaymentGateway gateway) {
        this.customers = customers;
        this.orders = orders;
        this.eventSeats = eventSeats;
        this.seats = seats;
        this.tiers = tiers;
        this.gateway = gateway;
    }

    @Transactional(rollbackFor = PaymentDeclinedException.class)
    public Long placeOrder(CheckoutService.Request r) throws PaymentDeclinedException {
        Customer customer = customers.findByEmail(r.email()).orElseThrow();
        List<EventSeat> grabbed = eventSeats.grabAvailable(r.eventId(), r.seats());
        if (grabbed.size() < r.seats()) throw new IllegalStateException("not enough seats");
        seats.findAllByIdIn(grabbed.stream().map(es -> es.getSeat().getId()).toList());   // one query instead of one per seat
        Map<Long, Money> priceBySector = tiers.findByEventId(r.eventId()).stream()
                .collect(Collectors.toMap(t -> t.getSector().getId(), PriceTier::getPrice));

        Order order = new Order(customer, "PLN");
        for (EventSeat es : grabbed) {
            es.sell();
            order.addTicket(es, customer.getFullName(), priceBySector.get(es.getSeat().getSector().getId()));
        }
        order.addPayment(gateway.charge(r.method(), r.paymentRef(), order.getTotal()));
        order.markPaid();
        return orders.save(order).getId();
    }
}
