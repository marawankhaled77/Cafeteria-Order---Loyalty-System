package cafeteria;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Order {
    private final String orderId = UUID.randomUUID().toString();
    private final String studentId;
    private final List<OrderLine> lines;
    private double total;
    private int pointsEarned;
    private OrderStatus status = OrderStatus.PLACED;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Order(String studentId, List<OrderLine> lines) {
        this.studentId = studentId;
        this.lines = new ArrayList<>(lines);
        this.total = lines.stream().mapToDouble(OrderLine::lineTotal).sum();
    }
    public String getOrderId() { return orderId; }
    public String getStudentId() { return studentId; }
    public List<OrderLine> getLines() { return lines; }
    public double getTotal() { return total; }
    public void setTotal(double t) { this.total = t; }
    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int p) { this.pointsEarned = p; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    @Override public String toString() {
        String items = lines.stream().map(l -> l.getItem().getName()+" x"+l.getQuantity()).collect(Collectors.joining(", "));
        return "["+status+"] Order "+orderId.substring(0,8)+" | Items: "+items+" | Total EGP "+String.format("%.2f",total)+" | Points "+pointsEarned+" | "+createdAt.toLocalTime();
    }
}
