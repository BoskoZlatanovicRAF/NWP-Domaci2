package example.repository;

import example.model.Student;
import framework.annotations.Component;
import framework.annotations.Qualifier;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll();
    Student findById(String id);
    void save(Student student);
}
