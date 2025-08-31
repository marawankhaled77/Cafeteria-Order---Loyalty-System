package cafeteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order o);
    List<Order> byStudent(String studentId);
    List<Order> byStatus(OrderStatus status);
    Optional<Order> findById(String orderId);
    Collection<Order> all();
}
