package example.controller;

import example.model.Student;
import example.service.StudentService;
import framework.annotations.*;
import framework.request.Request;
import framework.response.JsonResponse;
import framework.response.Response;

import java.util.Map;

@Controller
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GET
    @Path("/students")
    public Response getAllStudents(Request request) {
        return new JsonResponse(studentService.getAllStudents());
    }

    @GET
    @Path("/students/{id}")
    public Response getStudent(Request request) {
        String id = request.getParameter("id");
        Student student = studentService.getStudent(id);
        if (student == null) {
            return new JsonResponse(Map.of("error", "Student not found"));
        }
        return new JsonResponse(student);
    }

    @POST
    @Path("/students")
    public Response addStudent(Request request) {
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        int year = Integer.parseInt(request.getParameter("year"));

        Student student = new Student(id, name, year);
        studentService.addStudent(student);
        return new JsonResponse(student);
    }
}