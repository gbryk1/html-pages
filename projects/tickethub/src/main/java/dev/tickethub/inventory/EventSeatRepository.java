package dev.tickethub.inventory;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import java.util.List;

public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    long countByEventIdAndStatus(Long eventId, SeatStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select es from EventSeat es where es.id in :ids order by es.id")
    List<EventSeat> lockByIds(List<Long> ids);

    @NativeQuery("""
           select * from event_seat
           where event_id = :eventId and status = 'AVAILABLE'
           order by id
           limit :n
           for update skip locked
           """)
    List<EventSeat> grabAvailable(Long eventId, int n);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select es from EventSeat es where es.event.id = :eventId and es.status = 'AVAILABLE' order by es.id")
    List<EventSeat> grabAvailableJpql(Long eventId, Limit limit);
    @NativeQuery("""
           select * from event_seat
           where event_id = :eventId and status = 'AVAILABLE'
           order by id
           limit :n
           for update
           """)
    List<EventSeat> grabAvailableWaiting(Long eventId, int n);
}
