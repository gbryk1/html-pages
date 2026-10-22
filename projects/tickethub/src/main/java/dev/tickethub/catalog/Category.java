package dev.tickethub.catalog;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

@Entity
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NaturalId
    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    protected Category() {
    }

    public Category(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
