package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.service.ClassroomService;
import kr.pullgo.pullgoserver.service.spec.ClassroomSpecs;
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
public class ClassroomController {

    private final ClassroomService classroomService;

    @Autowired
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping("/academy/classrooms")
    public List<ClassroomDto.Result> search(
        @RequestParam(required = false) Long academyId,
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long applyingStudentId,
        @RequestParam(required = false) Long teacherId,
        @RequestParam(required = false) Long applyingTeacherId
    ) {
        Specification<Classroom> spec = null;
        if (academyId != null) {
            spec = ClassroomSpecs.belongsTo(academyId).and(spec);
        }
        if (studentId != null) {
            spec = ClassroomSpecs.hasStudent(studentId).and(spec);
        }
        if (applyingStudentId != null) {
            spec = ClassroomSpecs.hasApplyingStudent(applyingStudentId).and(spec);
        }
        if (teacherId != null) {
            spec = ClassroomSpecs.hasTeacher(teacherId).and(spec);
        }
        if (applyingTeacherId != null) {
            spec = ClassroomSpecs.hasApplyingTeacher(applyingTeacherId).and(spec);
        }

        return classroomService.search(spec);
    }

    @GetMapping("/academy/classrooms/{id}")
    public ClassroomDto.Result get(@PathVariable Long id) {
        return classroomService.read(id);
    }

    @PostMapping("/academy/classrooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassroomDto.Result post(@RequestBody ClassroomDto.Create dto) {
        return classroomService.create(dto);
    }

    @PatchMapping("/academy/classrooms/{id}")
    public ClassroomDto.Result patch(@PathVariable Long id, @RequestBody ClassroomDto.Update dto) {
        return classroomService.update(id, dto);
    }

    @DeleteMapping("/academy/classrooms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        classroomService.delete(id);
    }

    @PostMapping("/academy/classrooms/{id}/accept-teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptTeacher(@PathVariable Long id, @RequestBody ClassroomDto.AcceptTeacher dto) {
        classroomService.acceptTeacher(id, dto);
    }

    @PostMapping("/academy/classrooms/{id}/kick-teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickTeacher(@PathVariable Long id, @RequestBody ClassroomDto.KickTeacher dto) {
        classroomService.kickTeacher(id, dto);
    }


    @PostMapping("/academy/classrooms/{id}/accept-student")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptStudent(@PathVariable Long id, @RequestBody ClassroomDto.AcceptStudent dto) {
        classroomService.acceptStudent(id, dto);
    }

    @PostMapping("/academy/classrooms/{id}/kick-student")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickStudent(@PathVariable Long id, @RequestBody ClassroomDto.KickStudent dto) {
        classroomService.kickStudent(id, dto);
    }
}
