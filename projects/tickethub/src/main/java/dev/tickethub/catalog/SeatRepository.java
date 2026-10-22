package dev.tickethub.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("select s.id from Seat s where s.sector.venue.id = :venueId order by s.id")
    List<Long> findIdsByVenueId(Long venueId);

    long countBySectorVenueId(Long venueId);

    @Query("select s from Seat s where s.id in :ids")
    List<Seat> findAllByIdIn(List<Long> ids);
}
