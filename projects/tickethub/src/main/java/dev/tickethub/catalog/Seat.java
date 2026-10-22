package dev.tickethub.catalog;

import jakarta.persistence.*;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Sector sector;

    @Column(name = "row_label", nullable = false, length = 5)
    private String row;

    private int number;

    protected Seat() {
    }

    public Seat(Sector sector, String row, int number) {
        this.sector = sector;
        this.row = row;
        this.number = number;
    }

    public Long getId() { return id; }
    public Sector getSector() { return sector; }
    public String getRow() { return row; }
    public int getNumber() { return number; }
}
