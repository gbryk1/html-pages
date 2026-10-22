package dev.tickethub.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Service
public class PricingService {

    private final PriceTierRepository tiers;

    public PricingService(PriceTierRepository tiers) {
        this.tiers = tiers;
    }

    /** Admin form sends the version it showed; a stale version means someone changed the price meanwhile. */
    @Transactional
    public PriceTier changePrice(Long tierId, long expectedVersion, Money newPrice) {
        PriceTier tier = tiers.findById(tierId).orElseThrow();
        if (tier.getVersion() != expectedVersion) {
            throw new ObjectOptimisticLockingFailureException(PriceTier.class, tierId);
        }
        tier.setPrice(newPrice);
        return tier;
    }
}
