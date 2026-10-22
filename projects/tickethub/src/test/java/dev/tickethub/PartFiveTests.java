package dev.tickethub;

import dev.tickethub.catalog.*;
import dev.tickethub.inventory.*;
import dev.tickethub.sales.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.cache.use_second_level_cache=false")
class PartFiveTests {
    @Autowired TransactionTemplate tx;
    @Autowired Propagation propagation;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired EventRepository events;
    @Autowired CustomerRepository customers;
    @Autowired OrderRepository orders;
    @Autowired SeatImportService seatImport;
    @Autowired SeatInventoryService inventory;
    @Autowired EventSeatRepository eventSeats;
    @Autowired jakarta.persistence.EntityManager em;

    @Test
    void all() {
        try { propagation.outer(); System.out.println("PROP no exception"); }
        catch (Exception e) { System.out.println("PROP " + e.getClass().getName() + ": " + e.getMessage()); }

        Long[] ids = tx.execute(t -> {
            Venue v = new Venue("Hala", new Address("s", "Gdańsk", "80-000"));
            Sector a = v.addSector("A", SectorKind.SEATED, 10);
            venues.save(v);
            Event e = new Event("E", Instant.now(), v, categories.save(new Category("C5", "c")));
            e.openSale(); events.save(e);
            customers.save(new Customer("h@example.com", "H"));
            return new Long[]{a.getId(), e.getId()};
        });
        seatImport.importGrid(ids[0], 1, 4);
        inventory.generateFor(ids[1]);
        tx.executeWithoutResult(t -> {
            Order o = new Order(customers.findByEmail("h@example.com").orElseThrow(), "PLN");
            List<EventSeat> seats = eventSeats.grabAvailable(ids[1], 2);
            Set<Ticket> set = new HashSet<>();
            Ticket t1 = o.addTicket(seats.get(0), "H", Money.pln("10.00"));
            set.add(t1);
            orders.save(o);
            em.flush();
            System.out.println("EQ constant-hash contains after persist=" + set.contains(t1) + " id=" + t1.getId());
            // id-based hash simulation
            Map<Object, String> idHash = new HashMap<>();
            Object key = Objects.hash((Object) null);
            System.out.println("EQ idhash null=" + key + " after=" + Objects.hash(t1.getId()));
            o.addPayment(new BlikPayment(Money.pln("10.00"), "BLIK-1"));
            o.addPayment(new CardPayment(Money.pln("0.00"), "1234"));
        });
        System.out.println("P5MARK poly");
        tx.executeWithoutResult(t -> em.createQuery("select p from Payment p", Payment.class).getResultList()
                .forEach(p -> System.out.println("POLY " + p.getClass().getSimpleName() + " " + p.describe())));
        System.out.println("P5MARK end");
    }
}
