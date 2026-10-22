package dev.tickethub.catalog;

import jakarta.persistence.*;

@Entity
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Venue venue;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SectorKind kind;

    private int capacity;

    protected Sector() {
    }

    Sector(Venue venue, String name, SectorKind kind, int capacity) {
        this.venue = venue;
        this.name = name;
        this.kind = kind;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public Venue getVenue() { return venue; }
    public String getName() { return name; }
    public SectorKind getKind() { return kind; }
    public int getCapacity() { return capacity; }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Sector s && id != null && id.equals(s.getId());
    }

    @Override
    public int hashCode() {
        return Sector.class.hashCode();
    }
}
