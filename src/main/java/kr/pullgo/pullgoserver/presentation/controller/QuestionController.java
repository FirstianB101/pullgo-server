package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.service.QuestionService;
import kr.pullgo.pullgoserver.service.spec.QuestionSpecs;
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
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/exam/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto.Result post(@Valid @RequestBody QuestionDto.Create dto,
        Authentication authentication) {
        return questionService.create(dto, authentication);
    }

    @PostMapping("/exam/questions/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void post(@Valid @RequestBody List<QuestionDto.Create> dtos,
        Authentication authentication) {
        questionService.create(dtos, authentication);
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
    public void delete(@PathVariable Long id, Authentication authentication) {
        questionService.delete(id, authentication);
    }

    @PatchMapping("/exam/questions/{id}")
    public QuestionDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody QuestionDto.Update dto, Authentication authentication) {
        return questionService.update(id, dto, authentication);
    }

    @PatchMapping("/exam/questions/bulk")
    public void patch(
        @Valid @RequestBody Map<Long, QuestionDto.Update> dtos, Authentication authentication) {
        questionService.update(dtos, authentication);
    }
}
