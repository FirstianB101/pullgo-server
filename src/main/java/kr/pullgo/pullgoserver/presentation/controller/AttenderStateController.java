package kr.pullgo.pullgoserver.presentation.controller;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.service.AttenderStateService;
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
public class AttenderStateController {


    private final AttenderStateService attenderStateService;

    @Autowired
    public AttenderStateController(AttenderStateService attenderStateService) {
        this.attenderStateService = attenderStateService;
    }

    @PostMapping("/exam/attender-state")
    @ResponseStatus(HttpStatus.CREATED)
    public AttenderStateDto.Result post(@RequestBody AttenderStateDto.Create dto) {
        return attenderStateService.create(dto);
    }

    @GetMapping("/exam/attender-state")
    public List<AttenderStateDto.Result> list() {
        return attenderStateService.search();
    }

    @GetMapping("/exam/attender-state/{id}")
    public AttenderStateDto.Result get(@PathVariable Long id) {
        return attenderStateService.read(id);
    }

    @DeleteMapping("/exam/attender-state/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attenderStateService.delete(id);
    }

    @PatchMapping("/exam/attender-state/{id}")
    public AttenderStateDto.Result patch(@PathVariable Long id,
        @RequestBody AttenderStateDto.Update dto) {
        return attenderStateService.update(id, dto);
    }

    @PostMapping("/exam/attender-state/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submit(@PathVariable Long id) {
        attenderStateService.submit(id);
    }
}
