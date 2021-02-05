package kr.pullgo.pullgoserver.presentation.controller;


import java.util.List;
import java.util.Map;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.service.AcademyService;
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
public class AcademyController {

    private final AcademyService academyService;

    @Autowired
    public AcademyController(AcademyService academyService) {
        this.academyService = academyService;
    }

    @GetMapping("/academies")
    public List<AcademyDto.Result> list() {
        return academyService.getAcademies();
    }

    @GetMapping("/academies/{id}")
    public AcademyDto.Result get(@PathVariable Long id) {
        return academyService.getAcademy(id);
    }

    @PostMapping("/academies")
    @ResponseStatus(HttpStatus.CREATED)
    public AcademyDto.Result post(@RequestBody AcademyDto.Create dto) {
        return academyService.createAcademy(dto);
    }

    @PatchMapping("/academies/{id}")
    public AcademyDto.Result patch(@PathVariable Long id, @RequestBody AcademyDto.Update dto) {
        return academyService.updateAcademy(id, dto);
    }

    @DeleteMapping("/academies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        academyService.deleteAcademy(id);
    }

    @PostMapping("/academies/{id}/accept-teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptTeacher(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        academyService.acceptTeacher(id, (Long) body.get("teacherId"));
    }

    @PostMapping("/academies/{id}/kick-teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickTeacher(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        academyService.kickTeacher(id, (Long) body.get("teacherId"));
    }


    @PostMapping("/academies/{id}/accept-student")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptStudent(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        academyService.acceptStudent(id, (Long) body.get("studentId"));
    }

    @PostMapping("/academies/{id}/kick-student")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickStudent(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        academyService.kickStudent(id, (Long) body.get("studentId"));
    }
}
