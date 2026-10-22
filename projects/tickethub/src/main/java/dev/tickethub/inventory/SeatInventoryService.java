package dev.tickethub.inventory;

import dev.tickethub.catalog.Event;
import dev.tickethub.catalog.Seat;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatInventoryService {

    private static final int CHUNK = 50;

    private final EntityManagerFactory emf;

    public SeatInventoryService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** One event_seat row per physical seat of the venue; no persistence context, explicit batches. */
    public int generateFor(Long eventId) {
        try (StatelessSession ss = emf.unwrap(SessionFactory.class).openStatelessSession()) {
            Transaction tx = ss.beginTransaction();
            Event event = ss.get(Event.class, eventId);
            List<Seat> seats = ss.createSelectionQuery(
                            "from Seat s where s.sector.venue = :venue order by s.id", Seat.class)
                    .setParameter("venue", event.getVenue())
                    .getResultList();
            List<EventSeat> chunk = new ArrayList<>(CHUNK);
            for (Seat seat : seats) {
                chunk.add(new EventSeat(event, seat));
                if (chunk.size() == CHUNK) {
                    ss.insertMultiple(chunk);
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) ss.insertMultiple(chunk);
            tx.commit();
            return seats.size();
        }
    }
}
