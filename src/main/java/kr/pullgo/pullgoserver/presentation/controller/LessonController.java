package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.service.LessonService;
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
public class LessonController {

    private final LessonService lessonService;

    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/academy/classroom/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDto.Result post(@RequestBody LessonDto.Create dto) {
        return lessonService.createLesson(dto);
    }

    @GetMapping("/academy/classroom/lessons")
    public List<LessonDto.Result> list() {
        return lessonService.getLessons();
    }

    @GetMapping("/academy/classroom/lessons/{id}")
    public LessonDto.Result get(@PathVariable Long id) {
        return lessonService.getLesson(id);
    }

    @DeleteMapping("/academy/classroom/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        lessonService.deleteLesson(id);
    }

    @PatchMapping("/academy/classroom/lessons/{id}")
    public LessonDto.Result patch(@PathVariable Long id, @RequestBody LessonDto.Update dto) {
        return lessonService.updateLesson(id, dto);
    }

}
