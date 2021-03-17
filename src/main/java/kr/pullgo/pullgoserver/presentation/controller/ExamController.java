package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.service.ExamService;
import kr.pullgo.pullgoserver.service.spec.ExamSpecs;
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
public class ExamController {

    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/exams")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamDto.Result post(@RequestBody ExamDto.Create dto) {
        return examService.create(dto);
    }

    @GetMapping("/exams")
    public List<ExamDto.Result> search(
        @RequestParam(required = false) Long classroomId,
        @RequestParam(required = false) Long creatorId,
        @RequestParam(required = false) Long studentId,
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

        return examService.search(spec, pageable);
    }

    @GetMapping("/exams/{id}")
    public ExamDto.Result get(@PathVariable Long id) {
        return examService.read(id);
    }

    @DeleteMapping("/exams/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        examService.delete(id);
    }

    @PatchMapping("/exams/{id}")
    public ExamDto.Result patch(@PathVariable Long id, @RequestBody ExamDto.Update dto) {
        return examService.update(id, dto);
    }

    @PostMapping("/exams/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        examService.cancelExam(id);
    }

    @PostMapping("/exams/{id}/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finish(@PathVariable Long id) {
        examService.finishExam(id);
    }
}
