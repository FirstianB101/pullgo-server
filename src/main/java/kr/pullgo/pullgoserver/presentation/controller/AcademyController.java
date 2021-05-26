package kr.pullgo.pullgoserver.presentation.controller;


import java.util.List;
import javax.validation.Valid;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.service.AcademyService;
import kr.pullgo.pullgoserver.service.spec.AcademySpecs;
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
public class AcademyController {

    private final AcademyService academyService;

    @Autowired
    public AcademyController(AcademyService academyService) {
        this.academyService = academyService;
    }

    @GetMapping("/academies")
    public List<AcademyDto.Result> search(
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long applyingStudentId,
        @RequestParam(required = false) Long teacherId,
        @RequestParam(required = false) Long applyingTeacherId,
        @RequestParam(required = false) String nameLike,
        Pageable pageable
    ) {
        Specification<Academy> spec = null;
        if (ownerId != null) {
            spec = AcademySpecs.ownerId(ownerId).and(spec);
        }
        if (studentId != null) {
            spec = AcademySpecs.hasStudent(studentId).and(spec);
        }
        if (applyingStudentId != null) {
            spec = AcademySpecs.hasApplyingStudent(applyingStudentId).and(spec);
        }
        if (teacherId != null) {
            spec = AcademySpecs.hasTeacher(teacherId).and(spec);
        }
        if (applyingTeacherId != null) {
            spec = AcademySpecs.hasApplyingTeacher(applyingTeacherId).and(spec);
        }
        if (nameLike != null) {
            spec = AcademySpecs.nameLike("%" + nameLike + "%").and(spec);
        }

        return academyService.search(spec, pageable);
    }

    @GetMapping("/academies/{id}")
    public AcademyDto.Result get(@PathVariable Long id) {
        return academyService.read(id);
    }

    @PostMapping("/academies")
    @ResponseStatus(HttpStatus.CREATED)
    public AcademyDto.Result post(@Valid @RequestBody AcademyDto.Create dto,
        Authentication authentication) {
        return academyService.create(dto, authentication);
    }

    @PatchMapping("/academies/{id}")
    public AcademyDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody AcademyDto.Update dto, Authentication authentication) {
        return academyService.update(id, dto, authentication);
    }

    @DeleteMapping("/academies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        academyService.delete(id, authentication);
    }

    @PostMapping("/academies/{id}/accept-teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptTeacher(@PathVariable Long id,
        @Valid @RequestBody AcademyDto.AcceptTeacher dto, Authentication authentication) {
        academyService.acceptTeacher(id, dto, authentication);
    }

    @PostMapping("/academies/{id}/kick-teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickTeacher(@PathVariable Long id, @Valid @RequestBody AcademyDto.KickTeacher dto,
        Authentication authentication) {
        academyService.kickTeacher(id, dto, authentication);
    }


    @PostMapping("/academies/{id}/accept-student")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptStudent(@PathVariable Long id,
        @Valid @RequestBody AcademyDto.AcceptStudent dto, Authentication authentication) {
        academyService.acceptStudent(id, dto, authentication);
    }

    @PostMapping("/academies/{id}/kick-student")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickStudent(@PathVariable Long id, @Valid @RequestBody AcademyDto.KickStudent dto,
        Authentication authentication) {
        academyService.kickStudent(id, dto, authentication);
    }
}
