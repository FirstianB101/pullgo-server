package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto.Result;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.service.AttenderAnswerService;
import kr.pullgo.pullgoserver.service.spec.AttenderAnswerSpecs;
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
public class AttenderAnswerController {

    private final AttenderAnswerService attenderAnswerService;

    @Autowired
    public AttenderAnswerController(AttenderAnswerService attenderAnswerService) {
        this.attenderAnswerService = attenderAnswerService;
    }

    @PostMapping("/exam/attender-state/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public AttenderAnswerDto.Result post(@RequestBody AttenderAnswerDto.Create dto) {
        return attenderAnswerService.create(dto);
    }

    @GetMapping("/exam/attender-state/answers")
    public List<Result> search(
        @RequestParam(required = false) Long attenderStateId
    ) {
        Specification<AttenderAnswer> spec = null;
        if (attenderStateId != null) {
            spec = AttenderAnswerSpecs.belongsTo(attenderStateId).and(spec);
        }

        return attenderAnswerService.search(spec);
    }

    @GetMapping("/exam/attender-state/answers/{id}")
    public AttenderAnswerDto.Result get(@PathVariable Long id) {
        return attenderAnswerService.read(id);
    }

    @DeleteMapping("/exam/attender-state/answers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attenderAnswerService.delete(id);
    }

    @PatchMapping("/exam/attender-state/answers/{id}")
    public AttenderAnswerDto.Result patch(@PathVariable Long id,
        @RequestBody AttenderAnswerDto.Update dto) {
        return attenderAnswerService.update(id, dto);
    }

}
