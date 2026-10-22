package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BLIK")
public class BlikPayment extends Payment {

    @Column(name = "blik_ref", length = 40)
    private String blikRef;

    protected BlikPayment() {
    }

    public BlikPayment(Money amount, String blikRef) {
        super(amount);
        this.blikRef = blikRef;
    }

    @Override
    public String describe() { return "BLIK " + blikRef; }
}
