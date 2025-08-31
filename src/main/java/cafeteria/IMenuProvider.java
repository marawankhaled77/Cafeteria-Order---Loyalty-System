package cafeteria;
import java.util.List;

public interface IMenuProvider {
    List<MenuItem> getMenuItems();
    void addMenuItem(MenuItem item);
    void editMenuItem(int id, MenuItem updatedItem);
    void removeMenuItem(int id);
}
