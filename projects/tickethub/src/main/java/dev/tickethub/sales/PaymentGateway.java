package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import org.springframework.stereotype.Component;

@Component
public class PaymentGateway {

    /** Fake gateway: card numbers ending with 0000 are declined. */
    public Payment charge(String method, String ref, Money amount) throws PaymentDeclinedException {
        return switch (method) {
            case "CARD" -> {
                if (ref.endsWith("0000")) throw new PaymentDeclinedException("card declined");
                yield new CardPayment(amount, ref.substring(ref.length() - 4));
            }
            case "BLIK" -> new BlikPayment(amount, ref);
            case "VOUCHER" -> new VoucherPayment(amount, ref);
            default -> throw new IllegalArgumentException(method);
        };
    }
}
