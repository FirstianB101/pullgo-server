package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.service.TeacherService;
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
    public List<TeacherDto.Result> list() {
        return teacherService.search();
    }

    @GetMapping("teachers/{id}")
    public TeacherDto.Result get(@PathVariable Long id) {
        return teacherService.read(id);
    }

    @DeleteMapping("/teacher/{id}")
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
