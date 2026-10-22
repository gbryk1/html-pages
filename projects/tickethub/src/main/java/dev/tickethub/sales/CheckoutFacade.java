package dev.tickethub.sales;

import dev.tickethub.audit.AuditLogService;
import org.springframework.stereotype.Service;

/** No transaction here: the audit entry commits first, then checkout opens its own transaction. */
@Service
public class CheckoutFacade {

    private final AuditLogService audit;
    private final FastCheckoutService checkout;

    public CheckoutFacade(AuditLogService audit, FastCheckoutService checkout) {
        this.audit = audit;
        this.checkout = checkout;
    }

    public Long buy(CheckoutService.Request r) throws PaymentDeclinedException {
        audit.record("CHECKOUT_ATTEMPT", r.email() + " event=" + r.eventId());
        return checkout.placeOrder(r);
    }
}
