package dev.tickethub.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PriceTierRepository extends JpaRepository<PriceTier, Long> {
    Optional<PriceTier> findByEventIdAndSectorId(Long eventId, Long sectorId);

    List<PriceTier> findByEventId(Long eventId);
}
