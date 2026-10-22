package dev.tickethub.report;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public class SalesReport {

    public record Row(String sector, long sold, long available, BigDecimal revenue) {
    }

    private final JdbcClient jdbc;

    public SalesReport(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Row> forEvent(long eventId) {
        return jdbc.sql("""
                select s.name as sector,
                       count(*) filter (where es.status = 'SOLD')      as sold,
                       count(*) filter (where es.status = 'AVAILABLE') as available,
                       coalesce(sum(t.price_amount), 0)                as revenue
                from event_seat es
                join seat st   on st.id = es.seat_id
                join sector s  on s.id = st.sector_id
                left join ticket t on t.event_seat_id = es.id
                where es.event_id = :eventId
                group by s.name
                order by s.name
                """)
                .param("eventId", eventId)
                .query(Row.class)
                .list();
    }
}
