package example.repository;

import example.model.Student;
import framework.annotations.Component;
import framework.annotations.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("inMemory")
public class InMemoryStudentRepository implements StudentRepository {
    private final Map<String, Student> students = new HashMap<>();

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Student findById(String id) {
        return students.get(id);
    }

    @Override
    public void save(Student student) {
        students.put(student.getId(), student);
    }
}
