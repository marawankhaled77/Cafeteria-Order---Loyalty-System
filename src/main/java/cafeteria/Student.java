package cafeteria;

import java.util.*;

public class Student {
    private final String name;
    private final String studentId;
    private final String passwordHash;
    private int points;
    private double discountWallet; // EGP
    private final List<Order> orders = new ArrayList<>();

    public Student(String name, String studentId, String passwordHash) {
        this.name = name;
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.points = 0;
        this.discountWallet = 0.0;
    }
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public int getPoints() { return points; }

    public double getDiscountWallet() {
        return discountWallet;
    }

    public void addPoints(int p) { this.points += p; }
    public boolean deductPoints(int p) { if (points < p) return false; points -= p; return true; }
    public void addDiscount(double egp) { this.discountWallet += egp; }
    public double consumeDiscount(double total) {
        double applied = Math.min(discountWallet, total);
        discountWallet -= applied;
        return applied;
    }
    public List<Order> getOrders() { return orders; }
}
