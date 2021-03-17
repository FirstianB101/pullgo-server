package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.service.QuestionService;
import kr.pullgo.pullgoserver.service.spec.QuestionSpecs;
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
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/exam/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto.Result post(@Valid @RequestBody QuestionDto.Create dto) {
        return questionService.create(dto);
    }

    @GetMapping("/exam/questions")
    public List<QuestionDto.Result> search(
        @RequestParam(required = false) Long examId,
        Pageable pageable
    ) {
        Specification<Question> spec = null;
        if (examId != null) {
            spec = QuestionSpecs.belongsTo(examId).and(spec);
        }

        return questionService.search(spec, pageable);
    }

    @GetMapping("/exam/questions/{id}")
    public QuestionDto.Result get(@PathVariable Long id) {
        return questionService.read(id);
    }

    @DeleteMapping("/exam/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }

    @PatchMapping("/exam/questions/{id}")
    public QuestionDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody QuestionDto.Update dto) {
        return questionService.update(id, dto);
    }

}
