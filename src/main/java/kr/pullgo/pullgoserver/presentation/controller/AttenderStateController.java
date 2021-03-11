package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.service.AttenderStateService;
import kr.pullgo.pullgoserver.service.spec.AttenderStateSpecs;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AttenderStateController {


    private final AttenderStateService attenderStateService;

    @Autowired
    public AttenderStateController(AttenderStateService attenderStateService) {
        this.attenderStateService = attenderStateService;
    }

    @PostMapping("/exam/attender-states")
    @ResponseStatus(HttpStatus.CREATED)
    public AttenderStateDto.Result post(@RequestBody AttenderStateDto.Create dto) {
        return attenderStateService.create(dto);
    }

    @GetMapping("/exam/attender-states")
    public List<AttenderStateDto.Result> search(
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long examId
    ) {
        Specification<AttenderState> spec = null;
        if (studentId != null) {
            spec = AttenderStateSpecs.belongsToStudent(studentId).and(spec);
        }
        if (examId != null) {
            spec = AttenderStateSpecs.belongsToExam(examId).and(spec);
        }

        return attenderStateService.search(spec);
    }

    @GetMapping("/exam/attender-states/{id}")
    public AttenderStateDto.Result get(@PathVariable Long id) {
        return attenderStateService.read(id);
    }

    @DeleteMapping("/exam/attender-states/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attenderStateService.delete(id);
    }

    @PatchMapping("/exam/attender-states/{id}")
    public AttenderStateDto.Result patch(@PathVariable Long id,
        @RequestBody AttenderStateDto.Update dto) {
        return attenderStateService.update(id, dto);
    }

    @PostMapping("/exam/attender-states/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submit(@PathVariable Long id) {
        attenderStateService.submit(id);
    }
}
