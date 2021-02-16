package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.service.QuestionService;
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
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/exam/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto.Result post(@RequestBody QuestionDto.Create dto) {
        return questionService.createQuestion(dto);
    }

    @GetMapping("/exam/questions")
    public List<QuestionDto.Result> list() {
        return questionService.getQuestions();
    }

    @GetMapping("/exam/questions/{id}")
    public QuestionDto.Result get(@PathVariable Long id) {
        return questionService.getQuestion(id);
    }

    @DeleteMapping("/exam/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    @PatchMapping("/exam/questions/{id}")
    public QuestionDto.Result patch(@PathVariable Long id, @RequestBody QuestionDto.Update dto) {
        return questionService.updateQuestion(id, dto);
    }

}
