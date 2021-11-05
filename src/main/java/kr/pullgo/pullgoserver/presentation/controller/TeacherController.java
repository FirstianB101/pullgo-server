package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.service.TeacherService;
import kr.pullgo.pullgoserver.service.spec.TeacherSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class TeacherController {

    private final TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDto.Result post(@Valid @RequestBody TeacherDto.Create dto) {
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
    public void delete(@PathVariable Long id, Authentication authentication) {
        teacherService.delete(id, authentication);
    }

    @PatchMapping("/teachers/{id}")
    public TeacherDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody TeacherDto.Update dto, Authentication authentication) {
        return teacherService.update(id, dto, authentication);
    }

    @GetMapping("/teachers/{username}/exists")
    @ResponseStatus(HttpStatus.OK)
    public AccountDto.CheckDuplicationResult checkDuplicateUsername(@PathVariable String username) {
        return teacherService.checkDuplicateUsername(username);
    }

    @PostMapping("/teachers/{id}/apply-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyAcademy(@PathVariable Long id,
        @Valid @RequestBody TeacherDto.ApplyAcademy dto, Authentication authentication) {
        teacherService.applyAcademy(id, dto, authentication);
    }

    @PostMapping("/teachers/{id}/remove-applied-academy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedAcademy(@PathVariable Long id,
        @Valid @RequestBody TeacherDto.RemoveAppliedAcademy dto, Authentication authentication) {
        teacherService.removeAppliedAcademy(id, dto, authentication);
    }

    @PostMapping("/teachers/{id}/apply-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyClassroom(@PathVariable Long id,
        @Valid @RequestBody TeacherDto.ApplyClassroom dto, Authentication authentication) {
        teacherService.applyClassroom(id, dto, authentication);
    }

    @PostMapping("/teachers/{id}/remove-applied-classroom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAppliedClassroom(@PathVariable Long id,
        @Valid @RequestBody TeacherDto.RemoveAppliedClassroom dto, Authentication authentication) {
        teacherService.removeAppliedClassroom(id, dto, authentication);
    }
}
