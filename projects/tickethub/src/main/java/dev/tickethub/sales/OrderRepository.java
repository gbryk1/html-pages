package dev.tickethub.sales;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"tickets", "tickets.eventSeat"})
    Optional<Order> findWithTicketsById(Long id);


    @Query("select o from Order o join fetch o.payments where o in :orders")
    List<Order> fetchPayments(List<Order> orders);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Order o set o.status = dev.tickethub.sales.OrderStatus.CANCELLED where o.status = dev.tickethub.sales.OrderStatus.NEW and o.id in :ids")
    int cancelAll(List<Long> ids);
}
