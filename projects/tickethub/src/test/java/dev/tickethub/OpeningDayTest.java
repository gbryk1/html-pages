package dev.tickethub;

import dev.tickethub.audit.AuditLogRepository;
import dev.tickethub.catalog.*;
import dev.tickethub.inventory.*;
import dev.tickethub.sales.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.cache.use_second_level_cache=false"})
class OpeningDayTest {

    @Autowired TransactionTemplate tx;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired EventRepository events;
    @Autowired PriceTierRepository tiers;
    @Autowired CustomerRepository customers;
    @Autowired SeatImportService seatImport;
    @Autowired SeatInventoryService inventory;
    @Autowired EventSeatRepository eventSeats;
    @Autowired CheckoutService checkout;
    @Autowired CheckoutFacade facade;
    @Autowired AuditLogRepository auditLogs;
    @Autowired JdbcClient jdbc;
    @Autowired EntityManagerFactory emf;

    @Test
    void openingDay() throws Exception {
        Long[] ids = tx.execute(t -> {
            Venue v = new Venue("Stadion", new Address("s", "Warszawa", "00-001"));
            Sector a = v.addSector("A", SectorKind.SEATED, 40);
            venues.save(v);
            Event e = new Event("Otwarcie", Instant.now(), v, categories.save(new Category("CONCERT", "Koncert")));
            e.openSale();
            events.save(e);
            tiers.save(new PriceTier(e, a, Money.pln("199.00")));
            for (int i = 0; i < 30; i++) customers.save(new Customer("fan" + i + "@example.com", "Fan " + i));
            return new Long[]{a.getId(), e.getId()};
        });
        seatImport.importGrid(ids[0], 2, 20);   // 40 miejsc
        inventory.generateFor(ids[1]);

        var stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        facade.buy(new CheckoutService.Request("fan0@example.com", ids[1], 2, "BLIK", "B-0"));
        System.out.println("OPEN statements per checkout(2 seats)=" + stats.getPrepareStatementCount());

        ExecutorService pool = Executors.newFixedThreadPool(16);
        AtomicInteger ok = new AtomicInteger(), declined = new AtomicInteger(), soldOut = new AtomicInteger();
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 1; i < 30; i++) {
            int n = i;
            fs.add(pool.submit(() -> {
                String card = n % 7 == 0 ? "4000000000000000" : "41111111111111" + String.format("%02d", n);
                try {
                    facade.buy(new CheckoutService.Request("fan" + n + "@example.com", ids[1], 2, "CARD", card));
                    ok.incrementAndGet();
                } catch (PaymentDeclinedException e) {
                    declined.incrementAndGet();
                } catch (IllegalStateException e) {
                    soldOut.incrementAndGet();
                }
            }));
        }
        for (Future<?> f : fs) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();

        long sold = eventSeats.countByEventIdAndStatus(ids[1], SeatStatus.SOLD);
        long tickets = jdbc.sql("select count(*) from ticket").query(Long.class).single();
        long dupes = jdbc.sql("select count(*) from (select event_seat_id from ticket group by event_seat_id having count(*) > 1) d").query(Long.class).single();
        System.out.printf("OPEN ok=%d declined=%d soldOut=%d sold=%d tickets=%d dupes=%d audit=%d%n",
                ok.get() + 1, declined.get(), soldOut.get(), sold, tickets, dupes, auditLogs.count());
        assertThat(dupes).isZero();
        assertThat(sold).isEqualTo(tickets);
        assertThat(auditLogs.count()).isEqualTo(30);
    }
}
