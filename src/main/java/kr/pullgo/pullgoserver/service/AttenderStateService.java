package kr.pullgo.pullgoserver.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.dto.mapper.AttenderStateDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AttenderStateSubmitOnNoQuestionsOnExamException;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.service.authorizer.AttenderStateAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttenderStateService {

    private final AttenderStateDtoMapper dtoMapper;
    private final AttenderStateRepository attenderStateRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final AttenderStateAuthorizer attenderStateAuthorizer;

    @Autowired
    public AttenderStateService(AttenderStateDtoMapper dtoMapper,
        AttenderStateRepository attenderStateRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper,
        AttenderStateAuthorizer attenderStateAuthorizer) {
        this.dtoMapper = dtoMapper;
        this.attenderStateRepository = attenderStateRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
        this.attenderStateAuthorizer = attenderStateAuthorizer;
    }

    @Transactional
    public AttenderStateDto.Result create(AttenderStateDto.Create dto,
        Authentication authentication) {
        AttenderState attenderState = AttenderState.builder()
            .examStartTime(LocalDateTime.now())
            .build();

        Student dtoAttender = repoHelper.findStudentOrThrow(dto.getAttenderId());
        attenderStateAuthorizer.requireByOneself(authentication, dtoAttender);

        attenderState.setAttender(dtoAttender);

        Exam dtoExam = repoHelper.findExamOrThrow(dto.getExamId());
        attenderState.setExam(dtoExam);

        return dtoMapper.asResultDto(attenderStateRepository.save(attenderState));
    }

    @Transactional(readOnly = true)
    public AttenderStateDto.Result read(Long id) {
        AttenderState entity = repoHelper.findAttenderStateOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AttenderStateDto.Result> search(Specification<AttenderState> spec,
        Pageable pageable) {
        Page<AttenderState> entities = attenderStateRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        AttenderState entity = repoHelper.findAttenderStateOrThrow(id);
        attenderStateAuthorizer.requireOwningAttender(authentication, entity);

        attenderStateRepository.delete(entity);
    }

    @Transactional
    public void submit(Long id, Authentication authentication) {
        AttenderState attenderState = repoHelper.findAttenderStateOrThrow(id);
        attenderStateAuthorizer.requireOwningAttender(authentication, attenderState);

        Exam exam = attenderState.getExam();
        AttendingProgress presentAttendingProgress = attenderState.getProgress();

        if (presentAttendingProgress != AttendingProgress.ONGOING) {
            throw errorHelper.badRequest("Attender state already " + presentAttendingProgress);
        }
        if (attenderState.isAfterTimeLimit(attenderState.getExamStartTime())) {
            throw errorHelper.badRequest("Attender state submitted after timeout");
        }
        if (exam.isFinished()) {
            throw errorHelper.badRequest("Attender state already finished exam");
        }
        if (exam.isCancelled()) {
            throw errorHelper.badRequest("Attender state already cancelled exam");
        }
        if (attenderState.isOutOfTimeRange(attenderState.getExamStartTime())) {
            throw errorHelper.badRequest("Attender state submitted after time range");
        }
        try {
            attenderState.mark();
        } catch (AttenderStateSubmitOnNoQuestionsOnExamException e) {
            throw errorHelper.badRequest("There is no question in exam");
        }
    }

}
