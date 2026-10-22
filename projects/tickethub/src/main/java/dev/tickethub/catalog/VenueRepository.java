package dev.tickethub.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query("select v from Venue v where v.name = :name")
    List<Venue> findByName(String name);
}
