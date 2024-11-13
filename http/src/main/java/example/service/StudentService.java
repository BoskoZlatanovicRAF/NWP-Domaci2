package example.service;

import example.model.Student;
import example.repository.StudentRepository;
import framework.annotations.Autowired;
import framework.annotations.Qualifier;
import framework.annotations.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    @Qualifier("inMemory")
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudent(String id) {
        return studentRepository.findById(id);
    }

    public void addStudent(Student student) {
        studentRepository.save(student);
    }
}
