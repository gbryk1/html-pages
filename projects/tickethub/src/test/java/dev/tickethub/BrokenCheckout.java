package dev.tickethub;

import dev.tickethub.inventory.EventSeat;
import dev.tickethub.inventory.EventSeatRepository;
import dev.tickethub.sales.PaymentDeclinedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BrokenCheckout {

    private final EventSeatRepository eventSeats;

    public BrokenCheckout(EventSeatRepository eventSeats) {
        this.eventSeats = eventSeats;
    }

    @Transactional   // brak rollbackFor
    public void sellThenDecline(Long eventId, int n) throws PaymentDeclinedException {
        List<EventSeat> seats = eventSeats.grabAvailable(eventId, n);
        seats.forEach(EventSeat::sell);
        throw new PaymentDeclinedException("card declined");
    }

    public void outer(Long eventId) {
        inner(eventId);   // self-invocation
    }

    @Transactional
    public void inner(Long eventId) {
        System.out.println("SELFINV active=" + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
    }
}
