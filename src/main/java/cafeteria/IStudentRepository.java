package cafeteria;

public interface IStudentRepository {
    Student getStudentById(String studentId);
    void registerStudent(Student student);
    boolean login(String studentId, String password);
    void updatePoints(String studentId, int points);
}
