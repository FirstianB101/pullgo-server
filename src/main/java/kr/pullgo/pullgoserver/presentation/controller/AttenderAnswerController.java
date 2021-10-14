package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto.Result;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.service.AttenderAnswerService;
import kr.pullgo.pullgoserver.service.spec.AttenderAnswerSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/exam/attender-state/answers")
    public List<Result> search(
        @RequestParam(required = false) Long attenderStateId,
        Pageable pageable
    ) {
        Specification<AttenderAnswer> spec = null;
        if (attenderStateId != null) {
            spec = AttenderAnswerSpecs.belongsTo(attenderStateId).and(spec);
        }

        return attenderAnswerService.search(spec, pageable);
    }

    @GetMapping("/exam/attender-state/{attenderStateId}/answers/{questionId}")
    public AttenderAnswerDto.Result get(@PathVariable Long attenderStateId,
        @PathVariable Long questionId) {
        return attenderAnswerService.read(attenderStateId, questionId);
    }

    @DeleteMapping("/exam/attender-state/{attenderStateId}/answers/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long attenderStateId, @PathVariable Long questionId,
        Authentication authentication) {
        attenderAnswerService.delete(attenderStateId, questionId, authentication);
    }

    @PutMapping("/exam/attender-state/{attenderStateId}/answers/{questionId}")
    public ResponseEntity<AttenderAnswerDto.Result> put(@PathVariable Long attenderStateId,
        @PathVariable Long questionId,
        @Valid @RequestBody AttenderAnswerDto.Put dto, Authentication authentication) {
        return attenderAnswerService.put(attenderStateId, questionId, dto, authentication);
    }

}
