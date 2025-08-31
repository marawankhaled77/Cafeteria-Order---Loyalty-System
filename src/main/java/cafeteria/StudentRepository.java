package cafeteria;

import java.util.Collection;
import java.util.Optional;

public interface StudentRepository {
    Optional<Student> findById(String studentId);
    void save(Student s);
    boolean exists(String studentId);
    Collection<Student> all();
}
