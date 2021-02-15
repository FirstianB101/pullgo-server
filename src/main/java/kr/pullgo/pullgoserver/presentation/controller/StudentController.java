package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDto.Result post(@RequestBody StudentDto.Create dto) {
        return studentService.createStudent(dto);
    }

    @GetMapping("/students")
    public List<StudentDto.Result> list() {
        return studentService.getStudents();
    }

    @GetMapping("students/{id}")
    public StudentDto.Result get(@PathVariable Long id) {
        return studentService.getStudent(id);
    }

    @DeleteMapping("/student/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }

    @PatchMapping("/students/{id}")
    public StudentDto.Result patch(@PathVariable Long id, @RequestBody StudentDto.Update dto) {
        return studentService.updateStudent(id, dto);
    }

    @PostMapping("/students/{id}/apply-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyAcademy(@PathVariable Long id, @RequestBody StudentDto.ApplyAcademy dto) {
        studentService.applyAcademy(id, dto);
    }

    @PostMapping("/students/{id}/remove-applied-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedAcademy(@PathVariable Long id,
        @RequestBody StudentDto.RemoveAppliedAcademy dto) {
        studentService.removeAppliedAcademy(id, dto);
    }

    @PostMapping("/students/{id}/apply-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyClassroom(@PathVariable Long id, @RequestBody StudentDto.ApplyClassroom dto) {
        studentService.applyClassroom(id, dto);
    }

    @PostMapping("/students/{id}/remove-applied-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedClassroom(@PathVariable Long id,
        @RequestBody StudentDto.RemoveAppliedClassroom dto) {
        studentService.removeAppliedClassroom(id, dto);
    }
}
