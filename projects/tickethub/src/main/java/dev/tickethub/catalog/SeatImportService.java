package dev.tickethub.catalog;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatImportService {

    private static final int BATCH = 50; // = hibernate.jdbc.batch_size

    private final EntityManager em;

    public SeatImportService(EntityManager em) {
        this.em = em;
    }

    /** Creates rows x perRow seats in a sector; flush + clear every BATCH keeps the persistence context small. */
    @Transactional
    public int importGrid(Long sectorId, int rows, int perRow) {
        Sector sector = em.getReference(Sector.class, sectorId);
        int n = 0;
        for (int r = 0; r < rows; r++) {
            String row = rowLabel(r);
            for (int k = 1; k <= perRow; k++) {
                em.persist(new Seat(sector, row, k));
                if (++n % BATCH == 0) {
                    em.flush();
                    em.clear();
                    sector = em.getReference(Sector.class, sectorId);
                }
            }
        }
        return n;
    }

    static String rowLabel(int r) {
        return r < 26 ? String.valueOf((char) ('A' + r)) : "R" + (r + 1);
    }
}
