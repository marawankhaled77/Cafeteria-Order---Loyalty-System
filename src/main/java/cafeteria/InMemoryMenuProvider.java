package cafeteria;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMenuProvider implements MenuProvider {
    private static final String FILE = "menu.txt";
    private final Map<String, MenuItem> items = new ConcurrentHashMap<>();

    public InMemoryMenuProvider() {
        load();
    }

    @Override
    public Map<String, MenuItem> getMenu() {
        return items;
    }

    @Override
    public Optional<MenuItem> findById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void addItem(MenuItem item) {
        items.put(item.getId(), new PersistentMenuItem(item, this));
        saveToFile();
    }

    @Override
    public void removeItem(String id) {
        items.remove(id);
        saveToFile();
    }

    private void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length >= 5) {
                    String id = p[0];
                    String name = p[1];
                    String description = p[2];
                    double price = Double.parseDouble(p[3]);
                    String category = p[4];
                    items.put(id, new PersistentMenuItem(id, name, description, price, category, this));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (MenuItem item : items.values()) {
                pw.println(
                        item.getId() + ";" +
                                item.getName() + ";" +
                                item.getDescription() + ";" +
                                item.getPrice() + ";" +
                                item.getCategory()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Persistent version of MenuItem that auto-saves when edited
    private static class PersistentMenuItem extends MenuItem {
        private final InMemoryMenuProvider provider;

        public PersistentMenuItem(String id, String name, String description, double price, String category, InMemoryMenuProvider provider) {
            super(id, name, description, price, category);
            this.provider = provider;
        }

        public PersistentMenuItem(MenuItem base, InMemoryMenuProvider provider) {
            super(base.getId(), base.getName(), base.getDescription(), base.getPrice(), base.getCategory());
            this.provider = provider;
        }

        @Override
        public void setName(String n) {
            super.setName(n);
            provider.saveToFile();
        }

        @Override
        public void setPrice(double p) {
            super.setPrice(p);
            provider.saveToFile();
        }
    }
}