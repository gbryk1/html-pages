package dev.tickethub;

import dev.tickethub.catalog.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.properties.hibernate.cache.use_second_level_cache=false",
        "logging.level.org.hibernate.orm.jdbc.batch=trace"})
class PartThreeTests {

    @Autowired TransactionTemplate tx;
    @Autowired EntityManager em;
    @Autowired VenueRepository venues;
    @Autowired CategoryRepository categories;
    @Autowired PerformerRepository performers;
    @Autowired EventRepository events;
    @Autowired SeatImportService seatImport;
    @LocalServerPort int port;

    @Test
    void all() throws Exception {
        Long[] ids = tx.execute(t -> {
            Venue v = new Venue("Hala", new Address("s", "Gdańsk", "80-000"));
            Sector a = v.addSector("A", SectorKind.SEATED, 10);
            v.addSector("B", SectorKind.SEATED, 10);
            venues.save(v);
            Category c = categories.save(new Category("C3", "c"));
            Performer p1 = performers.save(new Performer("P1", "pop"));
            Performer p2 = performers.save(new Performer("P2", "pop"));
            Long first = null;
            for (int i = 0; i < 6; i++) {
                Event e = new Event("E" + i, Instant.now().plusSeconds(i), v, c);
                e.addPerformer(p1, 1); e.addPerformer(p2, 2); e.openSale();
                events.save(e);
                if (first == null) first = e.getId();
            }
            return new Long[]{v.getId(), a.getId(), first};
        });
        System.out.println("P3MARK batch");
        seatImport.importGrid(ids[1], 2, 30);
        System.out.println("P3MARK bag");
        try {
            tx.executeWithoutResult(t -> em.createQuery("select e from Event e join fetch e.lineup join fetch e.venue v join fetch v.sectors", Event.class).getResultList());
            System.out.println("BAG: none");
        } catch (Exception ex) {
            Throwable r = ex; while (r.getCause() != null) r = r.getCause();
            System.out.println("BAG: " + ex.getClass().getSimpleName() + " / " + r.getClass().getSimpleName() + ": " + r.getMessage());
        }
        System.out.println("P3MARK twostep");
        tx.executeWithoutResult(t -> {
            Event e = events.findWithLineup(ids[2]).orElseThrow();
            venues.findById(e.getVenue().getId());
            Venue v = events.fetchSectors(e.getVenue()).orElseThrow();
            System.out.println("TWOSTEP lineup=" + e.getLineup().size() + " sectors=" + v.getSectors().size());
        });
        System.out.println("P3MARK pagefetch");
        var page = events.pageWithLineup(EventStatus.ON_SALE, PageRequest.of(0, 2));
        System.out.println("PAGEFETCH size=" + page.getContent().size() + " total=" + page.getTotalElements());
        System.out.println("P3MARK osiv");
        HttpResponse<String> resp = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/raw/events/" + ids[2])).build(), HttpResponse.BodyHandlers.ofString());
        System.out.println("OSIV status=" + resp.statusCode() + " body=" + resp.body().substring(0, Math.min(200, resp.body().length())));
        System.out.println("P3MARK end");
    }
}
