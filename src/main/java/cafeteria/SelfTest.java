package cafeteria;

import java.util.*;

public class SelfTest {
    public static void main(String[] args) {
       FileStudentRepository studentRepo = new FileStudentRepository();
        InMemoryMenuProvider menuProvider = new InMemoryMenuProvider();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository("order.txt");
        PointsCalculator calculator = new BasicPointsCalculator(10.0);
        LoyaltyProgram loyalty = new LoyaltyProgram(calculator, studentRepo);
        NotificationService notifications = new NotificationService();
        OrderProcessor orders = new OrderProcessor(menuProvider, orderRepo, loyalty, notifications);
        MenuManager menuMgr = new MenuManager(menuProvider);
        StudentManager studentMgr = new StudentManager(studentRepo);

        menuMgr.addItem(new MenuItem("D001","Coffee","Hot",30.0,"Drink"));
        studentMgr.register("Omar","2024001","pw");
        Student s = studentMgr.login("2024001","pw").orElseThrow();
        List<OrderLine> cart = List.of(new OrderLine(menuMgr.findById("D001").get(),2));
        orders.placeOrder(s.getStudentId(), cart, new CashPaymentProcessor());
        System.out.println("OK orders="+orders.ordersOf(s.getStudentId()).size()+" points="+loyalty.pointsOf(s.getStudentId()));
    }
}
