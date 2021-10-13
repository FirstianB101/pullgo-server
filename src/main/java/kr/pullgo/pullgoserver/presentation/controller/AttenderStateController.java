package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.service.AttenderStateService;
import kr.pullgo.pullgoserver.service.spec.AttenderStateSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    public AttenderStateDto.Result post(@Valid @RequestBody AttenderStateDto.Create dto,
        Authentication authentication) {
        return attenderStateService.create(dto, authentication);
    }

    @GetMapping("/exam/attender-states")
    public List<AttenderStateDto.Result> search(
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long examId,
        Pageable pageable
    ) {
        Specification<AttenderState> spec = null;
        if (studentId != null) {
            spec = AttenderStateSpecs.belongsToStudent(studentId).and(spec);
        }
        if (examId != null) {
            spec = AttenderStateSpecs.belongsToExam(examId).and(spec);
        }

        return attenderStateService.search(spec, pageable);
    }

    @GetMapping("/exam/attender-states/{id}")
    public AttenderStateDto.Result get(@PathVariable Long id) {
        return attenderStateService.read(id);
    }

    @DeleteMapping("/exam/attender-states/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        attenderStateService.delete(id, authentication);
    }

    @PatchMapping("/exam/attender-states/{id}")
    public AttenderStateDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody AttenderStateDto.Update dto, Authentication authentication) {
        return attenderStateService.update(id, dto, authentication);
    }

    @PostMapping("/exam/attender-states/{id}/submit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submit(@PathVariable Long id, Authentication authentication) {
        attenderStateService.submit(id, authentication);
    }
}
