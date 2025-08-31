package cafeteria;

public class MenuItem {
    private final String id;
    private String name;
    private String description;
    private double price;
    private String category;

    public MenuItem(String id, String name, String description, double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public void setName(String n) { this.name = n; }
    public void setPrice(double p) { this.price = p; }
    @Override public String toString() { return id + " | " + name + " | EGP " + String.format("%.2f", price) + " | " + category; }
}
