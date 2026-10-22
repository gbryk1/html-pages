package dev.tickethub.sales;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final EntityManager em;

    public CustomerService(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public Customer register(String email, String fullName) {
        Customer c = new Customer(email, fullName);
        em.persist(c);
        return c;
    }

    @Transactional
    public Customer renameDetached(Customer detached) {
        return em.merge(detached);
    }

    @Transactional
    public void rename(Long id, String fullName) {
        em.find(Customer.class, id).setFullName(fullName);
    }
}
