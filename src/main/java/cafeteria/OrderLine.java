package cafeteria;

public class OrderLine {
    private final MenuItem item;
    private final int quantity;
    public OrderLine(MenuItem item, int quantity) { this.item = item; this.quantity = quantity; }
    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public double lineTotal() { return item.getPrice() * quantity; }
    @Override public String toString() { return item.getName() + " x" + quantity + " = EGP " + String.format("%.2f", lineTotal()); }
}
