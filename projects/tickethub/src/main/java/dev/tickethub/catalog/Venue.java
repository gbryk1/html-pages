package dev.tickethub.catalog;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name")
    private List<Sector> sectors = new ArrayList<>();

    protected Venue() {
    }

    public Venue(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public Sector addSector(String name, SectorKind kind, int capacity) {
        Sector s = new Sector(this, name, kind, capacity);
        sectors.add(s);
        return s;
    }

    public void removeSector(Sector s) {
        sectors.remove(s);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Address getAddress() { return address; }
    public List<Sector> getSectors() { return sectors; }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Venue v && id != null && id.equals(v.getId());
    }

    @Override
    public int hashCode() {
        return Venue.class.hashCode();
    }
}
