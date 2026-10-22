package dev.tickethub.catalog;

import jakarta.persistence.*;

@Entity
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 40)
    private String genre;

    protected Performer() {
    }

    public Performer(String name, String genre) {
        this.name = name;
        this.genre = genre;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getGenre() { return genre; }
}
