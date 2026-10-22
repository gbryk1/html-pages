package dev.tickethub.catalog;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByStatusAndStartsAtAfterOrderByStartsAtAsc(EventStatus status, Instant after);

    List<Event> findByVenueAddressCityIgnoreCase(String city);

    @Query("""
           select distinct e from Event e
           join e.lineup l
           where l.performer.name = :name
           order by e.startsAt
           """)
    List<Event> findByPerformerName(@Param("name") String name);

    <T> List<T> findByStatus(EventStatus status, Class<T> type);

    @Query("""
           select new dev.tickethub.catalog.EventCard(e.id, e.title, e.startsAt, v.name, v.address.city)
           from Event e join e.venue v
           where e.status = :status
           """)
    Page<EventCard> findCards(@Param("status") EventStatus status, Pageable pageable);

    Slice<Event> findSliceByStatus(EventStatus status, Pageable pageable);

    Window<Event> findByStatusOrderByStartsAtAscIdAsc(EventStatus status, ScrollPosition position, Limit limit);

    List<Event> findAllByStatus(EventStatus status);

    @EntityGraph(attributePaths = {"venue", "lineup", "lineup.performer"})
    List<Event> findWithLineupByStatus(EventStatus status);

    @Query("select e from Event e join fetch e.venue where e.status = :status")
    List<Event> findWithVenueByStatus(@Param("status") EventStatus status);

    @EntityGraph(attributePaths = "venue")
    Optional<Event> findWithVenueById(Long id);
    @Query("select e from Event e join fetch e.lineup l join fetch l.performer where e.id = :id")
    Optional<Event> findWithLineup(@Param("id") Long id);

    @Query("select v from Venue v join fetch v.sectors where v = :venue")
    Optional<Venue> fetchSectors(@Param("venue") Venue venue);

    @Query(value = "select e from Event e join fetch e.lineup where e.status = :status",
           countQuery = "select count(e) from Event e where e.status = :status")
    Page<Event> pageWithLineup(@Param("status") EventStatus status, Pageable pageable);
}
