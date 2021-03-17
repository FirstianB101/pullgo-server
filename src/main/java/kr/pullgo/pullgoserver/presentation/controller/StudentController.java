package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.service.StudentService;
import kr.pullgo.pullgoserver.service.spec.StudentSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
    public StudentDto.Result post(@Valid @RequestBody StudentDto.Create dto) {
        return studentService.create(dto);
    }

    @GetMapping("/students")
    public List<StudentDto.Result> search(
        @RequestParam(required = false) Long academyId,
        @RequestParam(required = false) Long appliedAcademyId,
        @RequestParam(required = false) Long classroomId,
        @RequestParam(required = false) Long appliedClassroomId
    ) {
        Specification<Student> spec = null;
        if (academyId != null) {
            spec = StudentSpecs.isEnrolledInAcademy(academyId).and(spec);
        }
        if (appliedAcademyId != null) {
            spec = StudentSpecs.hasAppliedToAcademy(appliedAcademyId).and(spec);
        }
        if (classroomId != null) {
            spec = StudentSpecs.isEnrolledInClassroom(classroomId).and(spec);
        }
        if (appliedClassroomId != null) {
            spec = StudentSpecs.hasAppliedToClassroom(appliedClassroomId).and(spec);
        }

        return studentService.search(spec);
    }

    @GetMapping("students/{id}")
    public StudentDto.Result get(@PathVariable Long id) {
        return studentService.read(id);
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        studentService.delete(id);
    }

    @PatchMapping("/students/{id}")
    public StudentDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody StudentDto.Update dto) {
        return studentService.update(id, dto);
    }

    @PostMapping("/students/{id}/apply-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyAcademy(@PathVariable Long id,
        @Valid @RequestBody StudentDto.ApplyAcademy dto) {
        studentService.applyAcademy(id, dto);
    }

    @PostMapping("/students/{id}/remove-applied-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedAcademy(@PathVariable Long id,
        @Valid @RequestBody StudentDto.RemoveAppliedAcademy dto) {
        studentService.removeAppliedAcademy(id, dto);
    }

    @PostMapping("/students/{id}/apply-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyClassroom(@PathVariable Long id,
        @Valid @RequestBody StudentDto.ApplyClassroom dto) {
        studentService.applyClassroom(id, dto);
    }

    @PostMapping("/students/{id}/remove-applied-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedClassroom(@PathVariable Long id,
        @Valid @RequestBody StudentDto.RemoveAppliedClassroom dto) {
        studentService.removeAppliedClassroom(id, dto);
    }
}
