package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CARD")
public class CardPayment extends Payment {

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    protected CardPayment() {
    }

    public CardPayment(Money amount, String cardLast4) {
        super(amount);
        this.cardLast4 = cardLast4;
    }

    @Override
    public String describe() { return "karta **** " + cardLast4; }

    public String getCardLast4() { return cardLast4; }
}
