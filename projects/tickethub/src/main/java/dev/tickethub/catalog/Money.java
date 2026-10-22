package dev.tickethub.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public record Money(
        @Column(precision = 12, scale = 2) BigDecimal amount,
        @Column(length = 3) String currency) {

    public static Money pln(String amount) {
        return new Money(new BigDecimal(amount), "PLN");
    }

    public Money plus(Money other) {
        if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
        return new Money(amount.add(other.amount), currency);
    }

    public Money times(int n) {
        return new Money(amount.multiply(BigDecimal.valueOf(n)), currency);
    }
}
