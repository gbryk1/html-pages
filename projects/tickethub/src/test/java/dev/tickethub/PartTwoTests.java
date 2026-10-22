package dev.tickethub;

import dev.tickethub.catalog.*;
import dev.tickethub.inventory.*;
import dev.tickethub.sales.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.cache.use_second_level_cache=false")
class PartTwoTests {

    @Autowired TransactionTemplate tx;
    @Autowired EntityManager em;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired EventRepository events;
    @Autowired CustomerRepository customers;
    @Autowired OrderRepository orders;
    @Autowired SeatImportService seatImport;
    @Autowired SeatInventoryService inventory;
    @Autowired EventSeatRepository eventSeats;
    @Autowired BrokenCheckout broken;

    @Test
    void all() {
        Long[] ids = tx.execute(t -> {
            Venue v = new Venue("Hala", new Address("s", "Gdańsk", "80-000"));
            Sector a = v.addSector("A", SectorKind.SEATED, 10);
            venues.save(v);
            Category c = categories.save(new Category("C2", "c"));
            System.out.println("P2MARK getReference");
            Event e = new Event("E", Instant.now(), venues.getReferenceById(v.getId()), categories.getReferenceById(c.getId()));
            e.openSale();
            events.save(e);
            System.out.println("P2MARK getReference-end");
            customers.save(new Customer("x@example.com", "X"));
            return new Long[]{v.getId(), a.getId(), e.getId()};
        });
        seatImport.importGrid(ids[1], 1, 10);
        inventory.generateFor(ids[2]);

        System.out.println("P2MARK save-new");
        Order o = tx.execute(t -> {
            Order n = new Order(customers.findByEmail("x@example.com").orElseThrow(), "PLN");
            Order r = orders.save(n);
            System.out.println("SAVE-NEW same=" + (r == n));
            return r;
        });
        System.out.println("P2MARK save-detached");
        o.setStatus(OrderStatus.CANCELLED);
        Order merged = tx.execute(t -> orders.save(o));
        System.out.println("SAVE-DETACHED same=" + (merged == o) + " v=" + merged.getVersion());
        System.out.println("P2MARK projection");
        events.findCards(EventStatus.ON_SALE, PageRequest.of(0, 5));
        events.findByStatus(EventStatus.ON_SALE, EventSummary.class);
        System.out.println("P2MARK broken");
        assertThatThrownBy(() -> broken.sellThenDecline(ids[2], 3)).isInstanceOf(PaymentDeclinedException.class);
        System.out.println("BROKEN sold=" + eventSeats.countByEventIdAndStatus(ids[2], SeatStatus.SOLD));
        broken.outer(ids[2]);
        broken.inner(ids[2]);
        System.out.println("P2MARK cancelAll");
        tx.executeWithoutResult(t -> System.out.println("CANCELALL=" + orders.cancelAll(java.util.List.of(o.getId()))));
        System.out.println("P2MARK end");
    }
}
