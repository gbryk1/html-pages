package dev.tickethub;

import dev.tickethub.catalog.*;
import dev.tickethub.inventory.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.cache.use_second_level_cache=false")
class PartFourTests {
    @Autowired TransactionTemplate tx;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired EventRepository events;
    @Autowired SeatImportService seatImport;
    @Autowired SeatInventoryService inventory;
    @Autowired EventSeatRepository eventSeats;

    @Test
    void locks() throws Exception {
        Long[] ids = tx.execute(t -> {
            Venue v = new Venue("Hala", new Address("s", "Gdańsk", "80-000"));
            Sector a = v.addSector("A", SectorKind.SEATED, 10);
            venues.save(v);
            Event e = new Event("E", Instant.now(), v, categories.save(new Category("C4", "c")));
            e.openSale(); events.save(e);
            return new Long[]{a.getId(), e.getId()};
        });
        seatImport.importGrid(ids[0], 1, 6);
        inventory.generateFor(ids[1]);
        System.out.println("P4MARK lockByIds");
        tx.executeWithoutResult(t -> eventSeats.lockByIds(List.of(1L, 2L)));
        System.out.println("P4MARK waiting");
        CountDownLatch aLocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<?> a = pool.submit(() -> tx.executeWithoutResult(t -> {
            List<EventSeat> s = eventSeats.grabAvailableWaiting(ids[1], 2);
            s.forEach(EventSeat::sell);
            aLocked.countDown();
            try { release.await(); } catch (InterruptedException e) { throw new RuntimeException(e); }
        }));
        aLocked.await();
        long t0 = System.currentTimeMillis();
        Future<Integer> b = pool.submit(() -> tx.execute(t -> eventSeats.grabAvailableWaiting(ids[1], 2).size()));
        Thread.sleep(1500);
        System.out.println("WAIT b.done before A commit=" + b.isDone());
        release.countDown();
        a.get();
        System.out.println("WAIT b rows=" + b.get() + " waitedMs>=" + (System.currentTimeMillis() - t0));
        pool.shutdown();
    }
}
