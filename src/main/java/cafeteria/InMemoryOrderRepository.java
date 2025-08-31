package cafeteria;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {
    private final File file;
    private final Map<String, Order> map = new HashMap<>();

    public InMemoryOrderRepository(String filename) {
        this.file = new File(filename);
        load();
    }

    @Override
    public void save(Order o) {
        map.put(o.getOrderId(), o);
        saveAll();
    }

    @Override
    public List<Order> byStudent(String studentId) {
        return map.values().stream()
                .filter(o -> o.getStudentId().equals(studentId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> byStatus(OrderStatus status) {
        return map.values().stream()
                .filter(o -> o.getStatus() == status)
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(map.get(orderId));
    }

    @Override
    public Collection<Order> all() {
        return map.values();
    }

    /** ✅ new method to update status and persist immediately */
    public void updateStatus(String orderId, OrderStatus newStatus) {
        Order o = map.get(orderId);
        if (o != null) {
            o.setStatus(newStatus);
            saveAll();
        }
    }

    private void saveAll() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Order o : map.values()) {
                pw.println(o.getOrderId() + "|" +
                        o.getStudentId() + "|" +
                        o.getStatus() + "|" +
                        o.getTotal() + "|" +
                        o.getPointsEarned());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Order o = new Order(parts[1], new ArrayList<>());
                    o.setStatus(OrderStatus.valueOf(parts[2]));
                    o.setTotal(Double.parseDouble(parts[3]));
                    o.setPointsEarned(Integer.parseInt(parts[4]));

                    // ✅ restore original orderId from file
                    try {
                        Field f = Order.class.getDeclaredField("orderId");
                        f.setAccessible(true);
                        f.set(o, parts[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    map.put(parts[0], o);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void delete(String orderId) {
        map.remove(orderId);
        saveAll();
    }
}