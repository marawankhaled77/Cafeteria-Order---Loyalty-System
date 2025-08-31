package cafeteria;
import java.util.List;

public interface IOrderRepository {
    void saveOrder(Order order);
    List<Order> getOrdersByStudent(String studentId);
    List<Order> getPendingOrders();
    void updateOrderStatus(int orderId, String status);
}
