package cafeteria;

import java.io.*;
import java.util.*;

public class FileStudentRepository implements StudentRepository {
    private static final String FILE = "students.txt";
    private final Map<String, Student> map = new HashMap<>();

    public FileStudentRepository() {
        load();
    }

    @Override
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(map.get(studentId));
    }

    @Override
    public void save(Student s) {
        // ✅ Ensure student is always wrapped in PersistentStudent
        if (!(s instanceof PersistentStudent)) {
            Student wrapped = new PersistentStudent(
                    s.getName(),
                    s.getStudentId(),
                    s.getPasswordHash(),
                    this
            );
            wrapped.addPoints(s.getPoints());
            wrapped.addDiscount(s.getDiscountWallet());
            s = wrapped;
        }
        map.put(s.getStudentId(), s);
        saveToFile();
    }

    @Override
    public boolean exists(String studentId) {
        return map.containsKey(studentId);
    }

    @Override
    public Collection<Student> all() {
        return map.values();
    }

    private void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length >= 3) {
                    String name = p[0];
                    String studentId = p[1];
                    String passwordHash = p[2];
                    int points = (p.length >= 4) ? Integer.parseInt(p[3]) : 0;
                    double wallet = (p.length >= 5) ? Double.parseDouble(p[4]) : 0.0;

                    Student s = new PersistentStudent(name, studentId, passwordHash, this);
                    s.addPoints(points);
                    s.addDiscount(wallet);
                    map.put(studentId, s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Student s : map.values()) {
                pw.println(
                        s.getName() + ";" +
                                s.getStudentId() + ";" +
                                s.getPasswordHash() + ";" +
                                s.getPoints() + ";" +
                                s.getDiscountWallet()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Persistent wrapper class
    private static class PersistentStudent extends Student {
        private final FileStudentRepository repo;

        public PersistentStudent(String name, String studentId, String passwordHash, FileStudentRepository repo) {
            super(name, studentId, passwordHash);
            this.repo = repo;
        }

        @Override
        public void addPoints(int p) {
            super.addPoints(p);
            repo.save(this); // auto save
        }

        @Override
        public boolean deductPoints(int p) {
            boolean ok = super.deductPoints(p);
            if (ok) repo.save(this);
            return ok;
        }

        @Override
        public void addDiscount(double egp) {
            super.addDiscount(egp);
            repo.save(this);
        }

        @Override
        public double consumeDiscount(double total) {
            double used = super.consumeDiscount(total);
            repo.save(this);
            return used;
        }
    }
}