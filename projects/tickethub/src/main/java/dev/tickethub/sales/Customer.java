package dev.tickethub.sales;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;
import java.util.Objects;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NaturalId
    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 160)
    private String fullName;

    protected Customer() {
    }

    public Customer(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Customer c && Objects.equals(email, c.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
