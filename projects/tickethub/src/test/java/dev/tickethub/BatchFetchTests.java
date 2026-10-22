package dev.tickethub;

import dev.tickethub.catalog.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.default_batch_fetch_size=16",
        "spring.jpa.properties.hibernate.cache.use_second_level_cache=false"})
class BatchFetchTests {

    @Autowired EntityManagerFactory emf;
    @Autowired TransactionTemplate tx;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired PerformerRepository performers;
    @Autowired EventRepository events;

    @Test
    void batchFetch() {
        tx.executeWithoutResult(t -> {
            Category c = categories.save(new Category("X", "X"));
            Performer p1 = performers.save(new Performer("P1", "pop"));
            Performer p2 = performers.save(new Performer("P2", "pop"));
            Performer p3 = performers.save(new Performer("P3", "pop"));
            for (int v = 0; v < 3; v++) {
                Venue venue = venues.save(new Venue("V" + v, new Address("s", "c", "00-000")));
                for (int e = 0; e < 4; e++) {
                    Event ev = new Event("E" + v + e, Instant.now(), venue, c);
                    ev.addPerformer(p1, 1);
                    ev.addPerformer(e % 2 == 0 ? p2 : p3, 2);
                    ev.openSale();
                    events.save(ev);
                }
            }
        });
        Statistics s = emf.unwrap(SessionFactory.class).getStatistics();
        s.clear();
        tx.executeWithoutResult(t -> events.findAllByStatus(EventStatus.ON_SALE).forEach(e -> e.getVenue().getName()));
        long venue = s.getPrepareStatementCount();
        s.clear();
        tx.executeWithoutResult(t -> events.findAllByStatus(EventStatus.ON_SALE).forEach(e -> e.getLineup().forEach(l -> l.getPerformer().getName())));
        System.out.println("BATCHFETCH: venue=" + venue + " lineup=" + s.getPrepareStatementCount());
    }
}
