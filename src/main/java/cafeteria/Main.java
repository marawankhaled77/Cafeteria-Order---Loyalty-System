package cafeteria;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        // Seed repositories and services (DIP)
       FileStudentRepository studentRepo = new FileStudentRepository();
        InMemoryMenuProvider menuProvider = new InMemoryMenuProvider();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository("order.txt");
        PointsCalculator calculator = new BasicPointsCalculator(10.0); // 1 point / EGP 10
        LoyaltyProgram loyalty = new LoyaltyProgram(calculator, studentRepo);
        NotificationService notifications = new NotificationService();
        OrderProcessor orders = new OrderProcessor(menuProvider, orderRepo, loyalty, notifications);
        MenuManager menuMgr = new MenuManager(menuProvider);
        StudentManager studentMgr = new StudentManager(studentRepo);
        ReportService reports = new ReportService(orderRepo);

        seed(menuMgr);

        while (true) {
            System.out.println("\n=== University Cafeteria System ===");
            System.out.println("1) Student");
            System.out.println("2) Staff (Admin)");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1": studentFlow(studentMgr, menuMgr, orders, loyalty); break;
                case "2": adminFlow(menuMgr, orders, reports); break;
                case "0": System.out.println("Bye!"); return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private static void seed(MenuManager menuMgr) {
        menuMgr.addItem(new MenuItem("M001", "Chicken Shawarma", "Grilled chicken wrap", 75.0, "Main Course"));
        menuMgr.addItem(new MenuItem("M002", "Koshari Bowl", "Classic Egyptian mix", 55.0, "Main Course"));
        menuMgr.addItem(new MenuItem("D001", "Iced Coffee", "Cold brew", 35.0, "Drink"));
        menuMgr.addItem(new MenuItem("S001", "Chocolate Muffin", "Freshly baked", 20.0, "Snack"));
    }

    private static void studentFlow(StudentManager studentMgr, MenuManager menuMgr, OrderProcessor orders, LoyaltyProgram loyalty) {
        System.out.println("\n-- Student --");
        System.out.println("1) Register");
        System.out.println("2) Login");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        Optional<Student> loggedIn = Optional.empty();
        if ("1".equals(c)) {
            System.out.print("Name: "); String name = sc.nextLine();
            System.out.print("Student ID: "); String sid = sc.nextLine();
            System.out.print("Password: "); String pwd = sc.nextLine();
            try {
                Student s = studentMgr.register(name, sid, pwd);
                System.out.println("Registered. Please login.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                return;
            }
        }
        if ("2".equals(c) || "1".equals(c)) {
            System.out.print("Student ID: "); String sid = sc.nextLine();
            System.out.print("Password: "); String pwd = sc.nextLine();
            loggedIn = studentMgr.login(sid, pwd);
            if (loggedIn.isEmpty()) { System.out.println("Login failed."); return; }
        }
        Student s = loggedIn.get();
        System.out.println("Welcome, " + s.getName() + " | Points: " + s.getPoints());

        List<OrderLine> cart = new ArrayList<>();
        while (true) {
            System.out.println("\n-- Student Menu --");
            System.out.println("1) Browse Menu");
            System.out.println("2) View Cart & Total");
            System.out.println("3) Place Order");
            System.out.println("4) Redeem Points");
            System.out.println("5) My Orders");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String c2 = sc.nextLine().trim();
            switch (c2) {
                case "1":
                    menuMgr.getMenu().forEach((id, item) -> System.out.println(item));
                    System.out.print("Enter item ID to add (or blank to stop): ");
                    String id = sc.nextLine().trim();
                    if (id.isEmpty()) break;
                    MenuItem mi = menuMgr.findById(id).orElse(null);
                    if (mi == null) { System.out.println("Not found."); break; }
                    System.out.print("Qty: "); int qty = Integer.parseInt(sc.nextLine());
                    cart.add(new OrderLine(mi, qty));
                    System.out.println("Added.");
                    break;
                case "2":
                    double total = cart.stream().mapToDouble(OrderLine::lineTotal).sum();
                    cart.forEach(System.out::println);
                    System.out.println("Total: EGP " + total);
                    break;
                case "3":
                    if (cart.isEmpty()) { System.out.println("Cart empty."); break; }
                    System.out.println("Payment method: 1) Cash  2) Card");
                    String pm = sc.nextLine().trim();
                    PaymentProcessor pay = "2".equals(pm) ? new CardPaymentProcessor() : new CashPaymentProcessor();
                    Order placed = orders.placeOrder(s.getStudentId(), cart, pay);
                    System.out.println("Order placed. ID: " + placed.getOrderId() + " | Total: EGP " + placed.getTotal());
                    System.out.println("You earned " + placed.getPointsEarned() + " points. New balance: " + loyalty.pointsOf(s.getStudentId()));
                    cart.clear();
                    break;
                case "4":
                    System.out.println("Redeem Options: 1) 50 pts → EGP 10 discount, 2) 100 pts → Free Coffee");
                    String opt = sc.nextLine().trim();
                    if ("1".equals(opt)) {
                        boolean ok = loyalty.redeemDiscount(s.getStudentId(), 50, 10.0);
                        System.out.println(ok ? "Discount wallet credited EGP 10." : "Not enough points.");
                    } else if ("2".equals(opt)) {
                        boolean ok = loyalty.redeemFreeItem(s.getStudentId(), 100, "D001");
                        System.out.println(ok ? "Free coffee token ready. Add Iced Coffee to your cart." : "Not enough points.");
                    } else {
                        System.out.println("Invalid.");
                    }
                    break;
                case "5":
                    orders.ordersOf(s.getStudentId()).forEach(System.out::println);
                    break;
                case "0": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private static void adminFlow(MenuManager menuMgr, OrderProcessor orders, ReportService reports) {
        System.out.print("Admin username: "); String u = sc.nextLine();
        System.out.print("Password: "); String p = sc.nextLine();
        if (!"admin".equals(u) || !"admin123".equals(p)) { System.out.println("Denied."); return; }
        while (true) {
            System.out.println("\n-- Admin --");
            System.out.println("1) Menu CRUD");
            System.out.println("2) Pending Orders");
            System.out.println("3) Reports");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.println(menuMgr.getMenu().values().stream()
                            .sorted(Comparator.comparing(MenuItem::getId))
                            .map(MenuItem::toString).collect(Collectors.joining("\n")));
                    System.out.println("a) Add  e) Edit  r) Remove  (Enter to skip)");
                    String op = sc.nextLine().trim();
                    if ("a".equals(op)) {
                        System.out.print("ID: "); String id = sc.nextLine();
                        System.out.print("Name: "); String name = sc.nextLine();
                        System.out.print("Desc: "); String desc = sc.nextLine();
                        System.out.print("Price: "); double price = Double.parseDouble(sc.nextLine());
                        System.out.print("Category: "); String cat = sc.nextLine();
                        menuMgr.addItem(new MenuItem(id, name, desc, price, cat));
                    } else if ("e".equals(op)) {
                        System.out.print("ID to edit: "); String id = sc.nextLine();
                        menuMgr.findById(id).ifPresentOrElse(item -> {
                            System.out.print("New name ("+item.getName()+"): "); String n = sc.nextLine(); if(!n.isEmpty()) item.setName(n);
                            System.out.print("New price ("+item.getPrice()+"): "); String p2 = sc.nextLine(); if(!p2.isEmpty()) item.setPrice(Double.parseDouble(p2));
                            System.out.println("Updated.");
                        }, () -> System.out.println("Not found."));
                    } else if ("r".equals(op)) {
                        System.out.print("ID to remove: "); String id = sc.nextLine();
                        menuMgr.removeItem(id);
                    }
                    break;
                case "2":
                    List<Order> pend = orders.listByStatus(OrderStatus.PLACED);
                    List<Order> pend1 = orders.listByStatus(OrderStatus.PREPARING);
                     pend.addAll(pend1);

                    if (pend.isEmpty()) System.out.println("No pending.");
                    for (Order o : pend) {
                        System.out.println(o);
                        if (pend1.contains(o)){
                            System.out.println("r) Ready  (other: skip)");
                        }else {
                            System.out.println("u) Preparing  r) Ready  (other: skip)");
                        }

                        String s = sc.nextLine().trim();
                        if ("u".equals(s)) orders.updateStatus(o.getOrderId(), OrderStatus.PREPARING);
                        else if ("r".equals(s)) orders.updateStatus(o.getOrderId(), OrderStatus.READY_FOR_PICKUP);
                    }
                    break;
                case "3":
                    System.out.println("1) Daily  2) Weekly  3) Export CSV");
                    String r = sc.nextLine().trim();
                    if ("1".equals(r)) System.out.println(reports.dailySummary());
                    else if ("2".equals(r)) System.out.println(reports.weeklySummary());
                    else if ("3".equals(r)) { String path = reports.exportCsv("reports"); System.out.println("Exported: " + path); }
                    break;
                case "0": return;
                default: System.out.println("Invalid.");
            }
        }
    }
}
