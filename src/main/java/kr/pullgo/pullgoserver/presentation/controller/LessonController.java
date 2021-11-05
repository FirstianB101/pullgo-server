package kr.pullgo.pullgoserver.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.service.LessonService;
import kr.pullgo.pullgoserver.service.spec.LessonSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
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
public class LessonController {

    private final LessonService lessonService;

    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/academy/classroom/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDto.Result post(@Valid @RequestBody LessonDto.Create dto,
        Authentication authentication) {
        return lessonService.create(dto, authentication);
    }

    @GetMapping("/academy/classroom/lessons")
    public List<LessonDto.Result> search(
        @RequestParam(required = false) Long classroomId,
        @RequestParam(required = false) Long academyId,
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long teacherId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate,
        Pageable pageable
    ) {
        Specification<Lesson> spec = null;
        if (classroomId != null) {
            spec = LessonSpecs.belongsTo(classroomId).and(spec);
        }
        if (academyId != null) {
            spec = LessonSpecs.belongsToAcademy(academyId).and(spec);
        }
        if (studentId != null) {
            spec = LessonSpecs.isAssignedToStudent(studentId).and(spec);
        }
        if (teacherId != null) {
            spec = LessonSpecs.isAssignedToTeacher(teacherId).and(spec);
        }
        if (sinceDate != null) {
            spec = LessonSpecs.sinceDate(sinceDate).and(spec);
        }
        if (untilDate != null) {
            spec = LessonSpecs.untilDate(untilDate).and(spec);
        }

        return lessonService.search(spec, pageable);
    }

    @GetMapping("/academy/classroom/lessons/{id}")
    public LessonDto.Result get(@PathVariable Long id) {
        return lessonService.read(id);
    }

    @DeleteMapping("/academy/classroom/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        lessonService.delete(id, authentication);
    }

    @PatchMapping("/academy/classroom/lessons/{id}")
    public LessonDto.Result patch(@PathVariable Long id, @Valid @RequestBody LessonDto.Update dto,
        Authentication authentication) {
        return lessonService.update(id, dto, authentication);
    }

}
