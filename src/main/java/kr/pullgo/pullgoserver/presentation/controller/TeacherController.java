package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.service.TeacherService;
import kr.pullgo.pullgoserver.service.spec.TeacherSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
public class TeacherController {

    private final TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDto.Result post(@RequestBody TeacherDto.Create dto) {
        return teacherService.create(dto);
    }

    @GetMapping("/teachers")
    public List<TeacherDto.Result> search(
        @RequestParam(required = false) Long academyId,
        @RequestParam(required = false) Long appliedAcademyId,
        @RequestParam(required = false) Long classroomId,
        @RequestParam(required = false) Long appliedClassroomId,
        Pageable pageable
    ) {
        Specification<Teacher> spec = null;
        if (academyId != null) {
            spec = TeacherSpecs.isEnrolledInAcademy(academyId).and(spec);
        }
        if (appliedAcademyId != null) {
            spec = TeacherSpecs.hasAppliedToAcademy(appliedAcademyId).and(spec);
        }
        if (classroomId != null) {
            spec = TeacherSpecs.isEnrolledInClassroom(classroomId).and(spec);
        }
        if (appliedClassroomId != null) {
            spec = TeacherSpecs.hasAppliedToClassroom(appliedClassroomId).and(spec);
        }

        return teacherService.search(spec, pageable);
    }

    @GetMapping("teachers/{id}")
    public TeacherDto.Result get(@PathVariable Long id) {
        return teacherService.read(id);
    }

    @DeleteMapping("/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teacherService.delete(id);
    }

    @PatchMapping("/teachers/{id}")
    public TeacherDto.Result patch(@PathVariable Long id, @RequestBody TeacherDto.Update dto) {
        return teacherService.update(id, dto);
    }

    @PostMapping("/teachers/{id}/apply-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyAcademy(@PathVariable Long id, @RequestBody TeacherDto.ApplyAcademy dto) {
        teacherService.applyAcademy(id, dto);
    }

    @PostMapping("/teachers/{id}/remove-applied-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedAcademy(@PathVariable Long id,
        @RequestBody TeacherDto.RemoveAppliedAcademy dto) {
        teacherService.removeAppliedAcademy(id, dto);
    }

    @PostMapping("/teachers/{id}/apply-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyClassroom(@PathVariable Long id, @RequestBody TeacherDto.ApplyClassroom dto) {
        teacherService.applyClassroom(id, dto);
    }

    @PostMapping("/teachers/{id}/remove-applied-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedClassroom(@PathVariable Long id,
        @RequestBody TeacherDto.RemoveAppliedClassroom dto) {
        teacherService.removeAppliedClassroom(id, dto);
    }
}
