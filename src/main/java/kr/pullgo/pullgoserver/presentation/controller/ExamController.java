package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.service.exam.ExamCrudService;
import kr.pullgo.pullgoserver.service.exam.ExamLifeCycleService;
import kr.pullgo.pullgoserver.service.spec.ExamSpecs;
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
public class ExamController {

    private final ExamCrudService examCrudService;
    private final ExamLifeCycleService examLifeCycleService;

    @Autowired
    public ExamController(ExamCrudService examCrudService,
        ExamLifeCycleService examLifeCycleService) {
        this.examCrudService = examCrudService;
        this.examLifeCycleService = examLifeCycleService;
    }

    @PostMapping("/exams")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamDto.Result post(@Valid @RequestBody ExamDto.Create dto,
        Authentication authentication) {
        return examService.create(dto, authentication);
    }

    @GetMapping("/exams")
    public List<ExamDto.Result> search(
        @RequestParam(required = false) Long classroomId,
        @RequestParam(required = false) Long creatorId,
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Boolean finished,
        @RequestParam(required = false) Boolean cancelled,
        Pageable pageable
    ) {
        Specification<Exam> spec = null;
        if (classroomId != null) {
            spec = ExamSpecs.belongsTo(classroomId).and(spec);
        }
        if (creatorId != null) {
            spec = ExamSpecs.isCreatedBy(creatorId).and(spec);
        }
        if (studentId != null) {
            spec = ExamSpecs.isAssignedTo(studentId).and(spec);
        }
        if (finished != null) {
            spec = ExamSpecs.isItFinished(finished).and(spec);
        }
        if (cancelled != null) {
            spec = ExamSpecs.isItCancelled(cancelled).and(spec);
        }
        return examService.search(spec, pageable);
    }

    @GetMapping("/exams/{id}")
    public ExamDto.Result get(@PathVariable Long id) {
        return examService.read(id);
    }

    @DeleteMapping("/exams/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        examService.delete(id, authentication);
    }

    @PatchMapping("/exams/{id}")
    public ExamDto.Result patch(@PathVariable Long id, @Valid @RequestBody ExamDto.Update dto,
        Authentication authentication) {
        return examService.update(id, dto, authentication);
    }

    @PostMapping("/exams/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, Authentication authentication) {
        examLifeCycleService.cancelExam(id, authentication);
    }

    @PostMapping("/exams/{id}/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finish(@PathVariable Long id, Authentication authentication) {
        examLifeCycleService.finishExam(id, authentication);
    }
}
