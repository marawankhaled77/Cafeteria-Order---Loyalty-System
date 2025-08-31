package cafeteria;

import java.util.*;

public class AcceptanceTest {
    public static void main(String[] args) {
        System.out.println("Starting acceptance test...");
        FileStudentRepository studentRepo = new FileStudentRepository();
        InMemoryMenuProvider menuProvider = new InMemoryMenuProvider();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository("order.txt");
        PointsCalculator calculator = new BasicPointsCalculator(10.0);
        LoyaltyProgram loyalty = new LoyaltyProgram(calculator, studentRepo);
        NotificationService notifications = new NotificationService();
        OrderProcessor orders = new OrderProcessor(menuProvider, orderRepo, loyalty, notifications);
        MenuManager menuMgr = new MenuManager(menuProvider);
        StudentManager studentMgr = new StudentManager(studentRepo);

        // seed
        menuMgr.addItem(new MenuItem("M001","Burger","Beef burger",40.0,"Main Course"));
        menuMgr.addItem(new MenuItem("S001","Fries","Crispy fries",15.0,"Snack"));
        menuMgr.addItem(new MenuItem("D001","Coffee","Hot coffee",20.0,"Drink"));

        // register + login
        studentMgr.register("Alice","2025001","pass");
        Student alice = studentMgr.login("2025001","pass").orElseThrow();

        // place order
        List<OrderLine> cart = List.of(new OrderLine(menuMgr.findById("M001").get(),1), new OrderLine(menuMgr.findById("D001").get(),1));
        Order placed = orders.placeOrder(alice.getStudentId(), cart, new CashPaymentProcessor());
        String orderId = placed.getOrderId();
        System.out.println("Placed order: " + orderId);

        // check points
        System.out.println("Points after order: " + loyalty.pointsOf(alice.getStudentId()));

        // redeem discount
        boolean redeemed = loyalty.redeemDiscount(alice.getStudentId(), 50, 10.0);
        System.out.println("Redeem 50 points for EGP10: " + redeemed);

        System.out.println("Acceptance test finished.");
    }
}
