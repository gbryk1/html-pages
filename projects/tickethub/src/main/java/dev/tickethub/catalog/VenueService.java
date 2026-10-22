package dev.tickethub.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueService {

    private final VenueRepository venues;

    public VenueService(VenueRepository venues) {
        this.venues = venues;
    }

    @Transactional
    public void rename(Long id, String name) {
        Venue v = venues.findById(id).orElseThrow();
        v.setName(name);
        venues.findByName(name);
    }

    @Transactional(readOnly = true)
    public void renameReadOnly(Long id, String name) {
        Venue v = venues.findById(id).orElseThrow();
        v.setName(name);
    }
}
