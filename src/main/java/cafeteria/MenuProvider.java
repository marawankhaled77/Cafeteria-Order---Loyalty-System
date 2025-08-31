package cafeteria;

import java.util.Map;
import java.util.Optional;

public interface MenuProvider {
    Map<String, MenuItem> getMenu();
    Optional<MenuItem> findById(String id);
    void addItem(MenuItem item);
    void removeItem(String id);
}
