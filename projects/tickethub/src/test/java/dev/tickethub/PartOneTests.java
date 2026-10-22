package dev.tickethub;

import dev.tickethub.catalog.*;
import dev.tickethub.sales.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.cache.use_second_level_cache=false")
class PartOneTests {

    @Autowired JdbcClient jdbc;
    @Autowired EntityManager em;
    @Autowired VenueRepository venues;
    @Autowired VenueService venueService;
    @Autowired CustomerService customerService;
    @Autowired TransactionTemplate tx;

    @Test
    @Transactional
    void threeWaysToTheSameRow() {
        jdbc.sql("insert into venue (id, name, street, city, postal_code) values (9001, 'Hala Olivia', 'Grunwaldzka 470', 'Gdańsk', '80-309')")
            .update();
        System.out.println("P1MARK jdbc-select");
        String viaJdbc = jdbc.sql("select name from venue where id = 9001").query(String.class).single();
        System.out.println("P1MARK em.find");
        Venue viaEm = em.find(Venue.class, 9001L);
        System.out.println("P1MARK findById");
        Venue viaRepo = venues.findById(9001L).orElseThrow();
        System.out.println("P1MARK end");
        assertThat(viaJdbc).isEqualTo(viaEm.getName());
        assertThat(viaRepo).isSameAs(viaEm);
    }

    @Test
    void lifecycleAndFlush() {
        System.out.println("P1MARK register");
        Customer c = customerService.register("zofia@example.com", "Zofia");
        System.out.println("P1MARK merge");
        c.setFullName("Zofia K.");
        Customer merged = customerService.renameDetached(c);
        assertThat(merged).isNotSameAs(c);
        System.out.println("P1MARK rename");
        customerService.rename(c.getId(), "Zofia Kowal");

        Long vid = tx.execute(t -> venues.save(new Venue("Hala A", new Address("s", "Gdańsk", "80-000"))).getId());
        System.out.println("P1MARK venue-rename");
        venueService.rename(vid, "Ergo Arena");
        System.out.println("P1MARK venue-readonly");
        venueService.renameReadOnly(vid, "Zmieniona");
        System.out.println("P1MARK end2");
        assertThat(venues.findById(vid).orElseThrow().getName()).isEqualTo("Ergo Arena");
    }
}
