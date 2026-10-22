package dev.tickethub;

import dev.tickethub.audit.AuditConfig;
import dev.tickethub.audit.AuditLogRepository;
import dev.tickethub.catalog.*;
import dev.tickethub.inventory.*;
import dev.tickethub.report.SalesReport;
import dev.tickethub.sales.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketHubTests {

    @Autowired EntityManagerFactory emf;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired PerformerRepository performers;
    @Autowired EventRepository events;
    @Autowired SeatRepository seats;
    @Autowired PriceTierRepository tiers;
    @Autowired EventSeatRepository eventSeats;
    @Autowired CustomerRepository customers;
    @Autowired OrderRepository orders;
    @Autowired AuditLogRepository auditLogs;
    @Autowired SeatImportService seatImport;
    @Autowired SeatInventoryService inventory;
    @Autowired PricingService pricing;
    @Autowired CheckoutService checkout;
    @Autowired EventSearch search;
    @Autowired SalesReport report;

    static Long eventId, venueId, sectorA, sectorB;

    Statistics stats() {
        Statistics s = emf.unwrap(SessionFactory.class).getStatistics();
        return s;
    }

    long countStatements(Runnable r) {
        Statistics s = stats();
        s.clear();
        r.run();
        return s.getPrepareStatementCount();
    }

    @Test @org.junit.jupiter.api.Order(1)
    void fixture() {
        tx.executeWithoutResult(t -> {
            Category concert = categories.save(new Category("CONCERT", "Koncert"));
            Category standup = categories.save(new Category("STANDUP", "Stand-up"));
            Performer p1 = performers.save(new Performer("Dawid Podsiadło", "pop"));
            Performer p2 = performers.save(new Performer("Sanah", "pop"));
            Performer p3 = performers.save(new Performer("Kabaret Moralnego Niepokoju", "kabaret"));
            Instant base = Instant.parse("2027-03-01T19:00:00Z");
            for (int v = 0; v < 3; v++) {
                Venue venue = new Venue("Hala " + v, new Address("Ulica " + v, v == 0 ? "Gdańsk" : "Kraków", "80-00" + v));
                Sector a = venue.addSector("A", SectorKind.SEATED, 200);
                venue.addSector("B", SectorKind.SEATED, 100);
                venues.save(venue);
                if (v == 0) { venueId = venue.getId(); }
                for (int e = 0; e < 4; e++) {
                    Event ev = new Event("Koncert " + v + "-" + e, base.plus(v * 10 + e, ChronoUnit.DAYS), venue, e % 2 == 0 ? concert : standup);
                    ev.addPerformer(p1, 1);
                    ev.addPerformer(e % 2 == 0 ? p2 : p3, 2);
                    ev.openSale();
                    events.save(ev);
                    if (v == 0 && e == 0) eventId = ev.getId();
                }
            }
            customers.save(new Customer("ala@example.com", "Ala Kowalska"));
            customers.save(new Customer("bartek@example.com", "Bartek Nowak"));
        });
        Venue v0 = tx.execute(t -> { Venue v = venues.findById(venueId).orElseThrow(); v.getSectors().size(); return v; });
        sectorA = v0.getSectors().get(0).getId();
        sectorB = v0.getSectors().get(1).getId();
        tx.executeWithoutResult(t -> {
            Event ev = events.getReferenceById(eventId);
            tiers.save(new PriceTier(ev, em.getReference(Sector.class, sectorA), Money.pln("250.00")));
            tiers.save(new PriceTier(ev, em.getReference(Sector.class, sectorB), Money.pln("180.00")));
        });
    }

    @Test @org.junit.jupiter.api.Order(2)
    void batchImport() {
        Statistics s = stats();
        s.clear();
        int n = seatImport.importGrid(sectorA, 10, 20) + seatImport.importGrid(sectorB, 5, 20);
        System.out.printf("BATCH: seats=%d inserts=%d prepared=%d flushes=%d%n",
                n, s.getEntityInsertCount(), s.getPrepareStatementCount(), s.getFlushCount());
        assertThat(seats.countBySectorVenueId(venueId)).isEqualTo(300);
    }

    @Test @org.junit.jupiter.api.Order(3)
    void statelessGenerate() {
        int n = inventory.generateFor(eventId);
        assertThat(n).isEqualTo(300);
        assertThat(eventSeats.countByEventIdAndStatus(eventId, SeatStatus.AVAILABLE)).isEqualTo(300);
    }

    @Test @org.junit.jupiter.api.Order(4)
    void persistenceContextIdentityAndDirtyChecking() {
        long st = countStatements(() -> tx.executeWithoutResult(t -> {
            Event a = events.findById(eventId).orElseThrow();
            Event b = events.findById(eventId).orElseThrow();
            assertThat(a).isSameAs(b);
            a.setStatus(EventStatus.SOLD_OUT); // no save()
        }));
        System.out.println("PC: statements=" + st);
        assertThat(events.findById(eventId).orElseThrow().getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        tx.executeWithoutResult(t -> events.findById(eventId).orElseThrow().setStatus(EventStatus.ON_SALE));
    }

    @Test @org.junit.jupiter.api.Order(5)
    void nPlusOne() {
        long bad = countStatements(() -> tx.executeWithoutResult(t ->
                events.findAllByStatus(EventStatus.ON_SALE).forEach(e -> e.getVenue().getName())));
        long fixed = countStatements(() -> tx.executeWithoutResult(t ->
                events.findWithVenueByStatus(EventStatus.ON_SALE).forEach(e -> e.getVenue().getName())));
        long graph = countStatements(() -> tx.executeWithoutResult(t ->
                events.findWithLineupByStatus(EventStatus.ON_SALE).forEach(e -> e.getLineup().forEach(l -> l.getPerformer().getName()))));
        long lineupBad = countStatements(() -> tx.executeWithoutResult(t ->
                events.findAllByStatus(EventStatus.ON_SALE).forEach(e -> e.getLineup().forEach(l -> l.getPerformer().getName()))));
        System.out.printf("N+1: venue bad=%d fixed=%d | lineup bad=%d graph=%d%n", bad, fixed, lineupBad, graph);
        assertThat(fixed).isEqualTo(1);
        assertThat(graph).isEqualTo(1);
    }

    @Test @org.junit.jupiter.api.Order(6)
    void multipleBag() {
        assertThatThrownBy(() -> tx.executeWithoutResult(t ->
                em.createQuery("select o from Order o join fetch o.tickets join fetch o.payments", dev.tickethub.sales.Order.class).getResultList()))
                .satisfies(ex -> System.out.println("BAG: " + ex.getClass().getName() + " / " + rootCause(ex).getClass().getName() + ": " + rootCause(ex).getMessage()));
    }

    static Throwable rootCause(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t;
    }

    @Test @org.junit.jupiter.api.Order(7)
    void paging() {
        long page = countStatements(() -> {
            Page<EventCard> p = events.findCards(EventStatus.ON_SALE, PageRequest.of(0, 5, Sort.by("startsAt", "id")));
            System.out.println("PAGE: total=" + p.getTotalElements() + " pages=" + p.getTotalPages() + " first=" + p.getContent().get(0));
        });
        long slice = countStatements(() -> {
            Slice<Event> s = events.findSliceByStatus(EventStatus.ON_SALE, PageRequest.of(0, 5, Sort.by("startsAt")));
            System.out.println("SLICE: hasNext=" + s.hasNext() + " size=" + s.getNumberOfElements());
        });
        System.out.printf("PAGING: page statements=%d slice statements=%d%n", page, slice);
        Window<Event> w = events.findByStatusOrderByStartsAtAscIdAsc(EventStatus.ON_SALE, ScrollPosition.keyset(), Limit.of(5));
        Window<Event> w2 = events.findByStatusOrderByStartsAtAscIdAsc(EventStatus.ON_SALE, w.positionAt(w.size() - 1), Limit.of(5));
        System.out.println("KEYSET: w1=" + w.size() + " hasNext=" + w.hasNext() + " w2first=" + w2.getContent().get(0).getTitle());
        assertThat(page).isEqualTo(2);
        assertThat(slice).isEqualTo(1);
    }

    @Test @org.junit.jupiter.api.Order(8)
    void specsAndQueries() {
        Page<Event> r = search.search(new EventSearch.Criteria("kraków", "CONCERT", null, null, null), PageRequest.of(0, 10));
        System.out.println("SPEC: " + r.getTotalElements());
        assertThat(r.getTotalElements()).isEqualTo(4);
        assertThat(events.findByPerformerName("Sanah")).hasSize(6);
        assertThat(events.findByStatus(EventStatus.ON_SALE, EventSummary.class)).hasSize(12);
        assertThat(events.findByVenueAddressCityIgnoreCase("GDAŃSK")).hasSize(4);
        assertThat(events.findByStatusAndStartsAtAfterOrderByStartsAtAsc(EventStatus.ON_SALE, Instant.parse("2027-03-20T00:00:00Z"))).hasSize(4);
    }

    @Test @org.junit.jupiter.api.Order(9)
    void optimisticLock() {
        PriceTier t0 = tiers.findByEventIdAndSectorId(eventId, sectorA).orElseThrow();
        long v = t0.getVersion();
        pricing.changePrice(t0.getId(), v, Money.pln("260.00"));
        assertThatThrownBy(() -> pricing.changePrice(t0.getId(), v, Money.pln("199.00")))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        // two concurrent transactions on the same row
        CountDownLatch bothRead = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<?>> fs = new ArrayList<>();
        for (String price : List.of("300.00", "310.00")) {
            fs.add(pool.submit(() -> tx.executeWithoutResult(t -> {
                PriceTier tier = tiers.findById(t0.getId()).orElseThrow();
                bothRead.countDown();
                try { bothRead.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { throw new RuntimeException(e); }
                tier.setPrice(Money.pln(price));
            })));
        }
        int failed = 0;
        for (Future<?> f : fs) {
            try { f.get(); } catch (ExecutionException e) {
                failed++;
                System.out.println("OPTIMISTIC: " + e.getCause().getClass().getName());
            } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
        pool.shutdown();
        assertThat(failed).isEqualTo(1);
    }

    @Test @org.junit.jupiter.api.Order(10)
    void skipLocked() throws Exception {
        CountDownLatch aLocked = new CountDownLatch(1), bDone = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<List<Long>> a = pool.submit(() -> tx.execute(t -> {
            List<Long> ids = eventSeats.grabAvailable(eventId, 5).stream().map(EventSeat::getId).toList();
            aLocked.countDown();
            try { bDone.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { throw new RuntimeException(e); }
            return ids;
        }));
        aLocked.await();
        Future<List<Long>> b = pool.submit(() -> tx.execute(t ->
                eventSeats.grabAvailableJpql(eventId, Limit.of(5)).stream().map(EventSeat::getId).toList()));
        List<Long> bIds = b.get(10, TimeUnit.SECONDS);
        bDone.countDown();
        List<Long> aIds = a.get(10, TimeUnit.SECONDS);
        pool.shutdown();
        System.out.println("SKIP: a=" + aIds + " b=" + bIds);
        assertThat(aIds).doesNotContainAnyElementsOf(bIds).hasSize(5);
        assertThat(bIds).hasSize(5);
    }

    @Test @org.junit.jupiter.api.Order(11)
    void checkoutAndRollback() {
        AuditConfig.CURRENT_USER.set("ala");
        try {
            long logsBefore = auditLogs.count();
            Long orderId = assertDoesNotThrowGet(() -> checkout.placeOrder(new CheckoutService.Request("ala@example.com", eventId, 2, "CARD", "4111111111111111")));
            assertThat(eventSeats.countByEventIdAndStatus(eventId, SeatStatus.SOLD)).isEqualTo(2);
            assertThatThrownBy(() -> checkout.placeOrder(new CheckoutService.Request("ala@example.com", eventId, 3, "CARD", "4000000000000000")))
                    .isInstanceOf(PaymentDeclinedException.class);
            assertThat(eventSeats.countByEventIdAndStatus(eventId, SeatStatus.SOLD)).isEqualTo(2); // rolled back
            assertThat(auditLogs.count()).isEqualTo(logsBefore + 2); // REQUIRES_NEW survived
            dev.tickethub.sales.Order o = tx.execute(t -> {
                dev.tickethub.sales.Order x = orders.findWithTicketsById(orderId).orElseThrow();
                x.getTickets().size();
                return x;
            });
            System.out.println("ORDER: total=" + o.getTotal() + " createdBy=" + o.getCreatedBy() + " createdAt=" + o.getCreatedAt() + " modBy=" + o.getLastModifiedBy());
            AuditConfig.CURRENT_USER.set("bartek");
            tx.executeWithoutResult(t -> orders.findById(orderId).orElseThrow().setStatus(OrderStatus.CANCELLED));
            List<Number> revs = tx.execute(t -> AuditReaderFactory.get(em).getRevisions(dev.tickethub.sales.Order.class, orderId));
            dev.tickethub.sales.Order first = tx.execute(t -> AuditReaderFactory.get(em).find(dev.tickethub.sales.Order.class, orderId, revs.get(0)));
            System.out.println("ENVERS: revisions=" + revs + " firstStatus=" + first.getStatus());
            assertThat(revs).hasSize(2);
            System.out.println("REPORT: " + report.forEvent(eventId));
        } finally {
            AuditConfig.CURRENT_USER.remove();
        }
    }

    interface ThrowingSupplier<T> { T get() throws Exception; }
    static <T> T assertDoesNotThrowGet(ThrowingSupplier<T> s) {
        try { return s.get(); } catch (Exception e) { throw new AssertionError(e); }
    }

    @Test @org.junit.jupiter.api.Order(12)
    void lazyOutsideTransaction() {
        Event e = events.findById(eventId).orElseThrow();
        assertThat(Hibernate.isInitialized(e.getVenue())).isFalse();
        assertThatThrownBy(() -> e.getVenue().getName()).isInstanceOf(LazyInitializationException.class)
                .satisfies(ex -> System.out.println("LAZY: " + ex.getMessage()));
        assertThat(e.getVenue().getId()).isNotNull(); // id of a proxy does not initialize it
        Event withVenue = events.findWithVenueById(eventId).orElseThrow();
        assertThat(withVenue.getVenue().getName()).isEqualTo("Hala 0");
    }

    @Test @org.junit.jupiter.api.Order(13)
    void secondLevelCache() {
        Long catId = categories.findByCode("CONCERT").orElseThrow().getId();
        Statistics s = stats();
        tx.executeWithoutResult(t -> em.find(Category.class, catId));
        s.clear();
        long st = countStatements(() -> tx.executeWithoutResult(t -> em.find(Category.class, catId)));
        System.out.printf("L2: statements=%d hits=%d%n", st, s.getSecondLevelCacheHitCount());
        assertThat(s.getSecondLevelCacheHitCount()).isPositive();
    }
}
