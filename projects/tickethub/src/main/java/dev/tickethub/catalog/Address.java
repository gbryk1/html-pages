package dev.tickethub.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String street,
        String city,
        @Column(name = "postal_code", length = 10) String postalCode) {
}
