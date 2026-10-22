package dev.tickethub.sales;

import dev.tickethub.catalog.Money;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VOUCHER")
public class VoucherPayment extends Payment {

    @Column(name = "voucher_code", length = 40)
    private String voucherCode;

    protected VoucherPayment() {
    }

    public VoucherPayment(Money amount, String voucherCode) {
        super(amount);
        this.voucherCode = voucherCode;
    }

    @Override
    public String describe() { return "voucher " + voucherCode; }
}
