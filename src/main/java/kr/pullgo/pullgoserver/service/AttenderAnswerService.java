package kr.pullgo.pullgoserver.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.mapper.AttenderAnswerDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.service.authorizer.AttenderAnswerAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttenderAnswerService {

    private final AttenderAnswerDtoMapper dtoMapper;
    private final AttenderAnswerRepository attenderAnswerRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final AttenderAnswerAuthorizer attenderAnswerAuthorizer;

    @Autowired
    public AttenderAnswerService(AttenderAnswerDtoMapper dtoMapper,
        AttenderAnswerRepository attenderAnswerRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper,
        AttenderAnswerAuthorizer attenderAnswerAuthorizer) {
        this.dtoMapper = dtoMapper;
        this.attenderAnswerRepository = attenderAnswerRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
        this.attenderAnswerAuthorizer = attenderAnswerAuthorizer;
    }

    @Transactional
    public AttenderAnswerDto.Result create(AttenderAnswerDto.Create dto,
        Authentication authentication) {
        AttenderState attenderState = repoHelper.findAttenderStateOrThrow(dto.getAttenderStateId());
        attenderAnswerAuthorizer.requireByOneself(authentication, attenderState.getAttender());
        checkAttenderStateValidations(attenderState);

        AttenderAnswer attenderAnswer = dtoMapper.asEntity(dto);

        Question question = repoHelper.findQuestionOrThrow(dto.getQuestionId());
        attenderAnswer.setQuestion(question);

        attenderState.addAnswer(attenderAnswer);

        return dtoMapper.asResultDto(attenderAnswerRepository.save(attenderAnswer));
    }

    @Transactional(readOnly = true)
    public AttenderAnswerDto.Result read(Long attenderStateId, Long questionId) {
        AttenderAnswer entity = repoHelper.findAttenderAnswerOrThrow(attenderStateId, questionId);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AttenderAnswerDto.Result> search(Specification<AttenderAnswer> spec,
        Pageable pageable) {
        Page<AttenderAnswer> entities = attenderAnswerRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public AttenderAnswerDto.Result update(Long attenderStateId, Long questionId,
        AttenderAnswerDto.Update dto,
        Authentication authentication) {
        AttenderAnswer entity = repoHelper.findAttenderAnswerOrThrow(attenderStateId, questionId);
        attenderAnswerAuthorizer.requireOwningAttender(authentication, entity);

        AttenderState attenderState = entity.getAttenderState();
        checkAttenderStateValidations(attenderState);

        if (dto.getAnswer() != null) {
            entity.setAnswer(new Answer(dto.getAnswer()));
        }
        return dtoMapper.asResultDto(attenderAnswerRepository.save(entity));
    }

    @Transactional
    public void delete(Long attenderStateId, Long questionId, Authentication authentication) {
        AttenderAnswer entity = repoHelper.findAttenderAnswerOrThrow(attenderStateId, questionId);
        attenderAnswerAuthorizer.requireOwningAttender(authentication, entity);

        entity.setAttenderState(null);
        attenderAnswerRepository.delete(entity);
    }

    public boolean exists(Long attenderStateId, Long questionId)
        throws ResponseStatusException {
        return attenderAnswerRepository.existsByAttenderStateIdAndQuestionId(
            attenderStateId, questionId);
    }

    private void checkAttenderStateValidations(AttenderState attenderState) {
        Exam exam = attenderState.getExam();

        AttendingProgress presentAttendingProgress = attenderState.getProgress();

        if (presentAttendingProgress != AttendingProgress.ONGOING) {
            throw errorHelper.badRequest("Attender state is already " + presentAttendingProgress);
        }
        if (attenderState.isAfterTimeLimit(LocalDateTime.now())) {
            throw errorHelper.badRequest("It's been updated beyond the timeout");
        }
        if (attenderState.isOutOfTimeRange(LocalDateTime.now())) {
            throw errorHelper.badRequest("It's been updated after time range");
        }
        if (exam.isFinished()) {
            throw errorHelper.badRequest("It's been updated already finished exam");
        }
        if (exam.isCancelled()) {
            throw errorHelper.badRequest("It's been updated already cancelled exam");
        }
    }
}
