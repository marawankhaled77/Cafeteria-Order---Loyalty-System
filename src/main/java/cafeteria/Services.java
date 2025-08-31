package cafeteria;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

class HashUtil {
    public static String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}

class StudentManager {
    private final StudentRepository repo;
    public StudentManager(StudentRepository repo) { this.repo = repo; }
    public Student register(String name, String studentId, String password) {
        if (repo.exists(studentId)) throw new IllegalArgumentException("Student ID already exists.");
        String hash = HashUtil.sha256(password);
        Student s = new Student(name, studentId, hash);
        repo.save(s);
        return s;
    }
    public Optional<Student> login(String studentId, String password) {
        String hash = HashUtil.sha256(password);
        return repo.findById(studentId).filter(s -> s.getPasswordHash().equals(hash));
    }
    public boolean exists(String studentId) { return repo.exists(studentId); }
}

class MenuManager {
    private final MenuProvider provider;
    public MenuManager(MenuProvider provider) { this.provider = provider; }
    public Map<String, MenuItem> getMenu() { return provider.getMenu(); }
    public Optional<MenuItem> findById(String id) { return provider.findById(id); }
    public void addItem(MenuItem i) { provider.addItem(i); }
    public void removeItem(String id) { provider.removeItem(id); }
}

interface PointsCalculator {
    int earnPoints(double amountAfterDiscount);
}

class BasicPointsCalculator implements PointsCalculator {
    private final double egpPerPoint;
    public BasicPointsCalculator(double egpPerPoint) { this.egpPerPoint = egpPerPoint; }
    public int earnPoints(double amount) { return (int)Math.floor(amount / egpPerPoint); }
}

class TieredPointsCalculator implements PointsCalculator {
    // Example: >200 EGP → 2x
    public int earnPoints(double amount) {
        double rate = amount >= 200 ? 5.0 : 10.0; // more generous for big orders
        return (int)Math.floor(amount / rate);
    }
}

class LoyaltyProgram {
    private final PointsCalculator calculator;
    private final StudentRepository students;
    public LoyaltyProgram(PointsCalculator calc, StudentRepository repo) { this.calculator = calc; this.students = repo; }
    // package-private accessor for repository (used by OrderProcessor within same package)
    StudentRepository getStudentRepository() { return students; }
    public int awardPoints(String studentId, double amountAfterDiscount) {
        int pts = calculator.earnPoints(amountAfterDiscount);
        students.findById(studentId).ifPresent(s -> s.addPoints(pts));
        return pts;
    }
    public boolean redeemDiscount(String studentId, int pointsCost, double egpDiscount) {
        Optional<Student> os = students.findById(studentId);
        if (os.isEmpty()) return false;
        Student s = os.get();
        if (!s.deductPoints(pointsCost)) return false;
        s.addDiscount(egpDiscount);
        return true;
    }
    public boolean redeemFreeItem(String studentId, int pointsCost, String menuItemId) {
        Optional<Student> os = students.findById(studentId);
        if (os.isEmpty()) return false;
        Student s = os.get();
        if (!s.deductPoints(pointsCost)) return false;
        // For simplicity: add discount equal to cheapest item price later at checkout when added
        return true;
    }
    public int pointsOf(String studentId) {
        return students.findById(studentId).map(Student::getPoints).orElse(0);
    }
}

class NotificationService {
    public void notifyReady(String studentId, String orderId) {
        System.out.println(">> Notification to "+studentId+": Your order "+orderId.substring(0,8)+" is READY_FOR_PICKUP.");
    }
}

class OrderProcessor {
    private final MenuProvider menu;
    private final OrderRepository orders;
    private final LoyaltyProgram loyalty;
    private final NotificationService notifications;
    private final Map<String, List<Order>> byStudentCache = new HashMap<>();

    public OrderProcessor(MenuProvider menu, OrderRepository repo, LoyaltyProgram loyalty, NotificationService notifications) {
        this.menu = menu; this.orders = repo; this.loyalty = loyalty; this.notifications = notifications;
    }

    public Order placeOrder(String studentId, List<OrderLine> cart, PaymentProcessor payment) {
        // Apply discount wallet & freebies (simplified: wallet consumed automatically)
        Order o = new Order(studentId, cart);
        double appliedDiscount = getStudent(studentId).map(s -> s.consumeDiscount(o.getTotal())).orElse(0.0);
        o.setTotal(o.getTotal() - appliedDiscount);
        if (!payment.process(o.getTotal())) throw new IllegalStateException("Payment failed.");
        int pts = loyalty.awardPoints(studentId, o.getTotal());
        o.setPointsEarned(pts);
        orders.save(o);
        getStudent(studentId).ifPresent(s -> s.getOrders().add(o));
        return o;
    }

    public List<Order> ordersOf(String studentId) { return orders.byStudent(studentId); }

    public List<Order> listByStatus(OrderStatus st) { return orders.byStatus(st); }

    public void updateStatus(String orderId, OrderStatus status) {
        orders.findById(orderId).ifPresent(o -> {
            o.setStatus(status);
            if (status == OrderStatus.READY_FOR_PICKUP) notifications.notifyReady(o.getStudentId(), o.getOrderId());
        });
    }

    private Optional<Student> getStudent(String id) {
    // Use package accessor to reach student repository without breaking encapsulation across packages
    return loyalty.getStudentRepository().findById(id);
    }
}

class ReportService {
    private final OrderRepository repo;
    public ReportService(OrderRepository repo) { this.repo = repo; }
    public String dailySummary() {
        LocalDate today = LocalDate.now();
        List<Order> list = repo.all().stream().filter(o -> o.getCreatedAt().toLocalDate().equals(today)).collect(Collectors.toList());
        double sales = list.stream().mapToDouble(Order::getTotal).sum();
        int redemptions = 0; // tracked via loyalty repo in a real DB
        return "Daily ("+today+"): orders="+list.size()+", sales=EGP "+String.format("%.2f",sales)+", redemptions~"+redemptions;
    }
    public String weeklySummary() {
        LocalDate start = LocalDate.now().minusDays(6);
        List<Order> list = repo.all().stream().filter(o -> !o.getCreatedAt().toLocalDate().isBefore(start)).collect(Collectors.toList());
        double sales = list.stream().mapToDouble(Order::getTotal).sum();
        return "Weekly ("+start+".."+LocalDate.now()+"): orders="+list.size()+", sales=EGP "+String.format("%.2f",sales);
    }
    public String exportCsv(String outDir) {
        try {
            Files.createDirectories(Paths.get(outDir));
            String name = "sales_"+LocalDate.now().toString()+".csv";
            Path p = Paths.get(outDir, name);
            String header = "orderId,studentId,total,status,createdAt\n";
            String rows = repo.all().stream().map(o -> String.join(",",
                    o.getOrderId(), o.getStudentId(), String.format("%.2f",o.getTotal()), o.getStatus().name(), o.getCreatedAt().toString()))
                    .collect(Collectors.joining("\n"));
            Files.writeString(p, header+rows);
            return p.toString();
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}

class CashPaymentProcessor implements PaymentProcessor {
    public boolean process(double amount) { return true; }
}
class CardPaymentProcessor implements PaymentProcessor {
    public boolean process(double amount) { return true; } // simulate success
}
