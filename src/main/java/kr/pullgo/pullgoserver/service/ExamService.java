package kr.pullgo.pullgoserver.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.mapper.ExamDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableScheduling
@Slf4j
public class ExamService {

    private final ExamDtoMapper dtoMapper;
    private final ExamRepository examRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final ExamAuthorizer examAuthorizer;

    @Autowired
    public ExamService(ExamDtoMapper dtoMapper,
        ExamRepository examRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper,
        ExamAuthorizer examAuthorizer) {
        this.dtoMapper = dtoMapper;
        this.examRepository = examRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
        this.examAuthorizer = examAuthorizer;
    }

    @Transactional
    public ExamDto.Result create(ExamDto.Create dto, Authentication authentication) {
        Exam exam = dtoMapper.asEntity(dto);

        Teacher creator = repoHelper.findTeacherOrThrow(dto.getCreatorId());
        examAuthorizer.requireByOneself(authentication, creator);
        exam.setCreator(creator);

        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());
        classroom.addExam(exam);

        return dtoMapper.asResultDto(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public ExamDto.Result read(Long id) {
        Exam entity = repoHelper.findExamOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<ExamDto.Result> search(Specification<Exam> spec, Pageable pageable) {
        Page<Exam> entities = examRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public ExamDto.Result update(Long id, ExamDto.Update dto, Authentication authentication) {
        Exam entity = repoHelper.findExamOrThrow(id);
        examAuthorizer.requireCreator(authentication, entity);

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getBeginDateTime() != null) {
            entity.setBeginDateTime(dto.getBeginDateTime());
        }
        if (dto.getEndDateTime() != null) {
            entity.setEndDateTime(dto.getEndDateTime());
        }
        if (dto.getTimeLimit() != null) {
            entity.setTimeLimit(dto.getTimeLimit());
        }
        if (dto.getPassScore() != null) {
            entity.setPassScore(dto.getPassScore());
        }
        return dtoMapper.asResultDto(examRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Exam entity = repoHelper.findExamOrThrow(id);
        examAuthorizer.requireCreator(authentication, entity);

        examRepository.delete(entity);
    }

    @Transactional
    public void cancelExam(Long id, Authentication authentication) {
        Exam exam = getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        exam.setCancelled(true);
    }

    @Transactional
    public void finishExam(Long id, Authentication authentication) {
        Exam exam = getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        finishExam(exam);
    }

    private void finishExam(Exam exam) {
        exam.getAttenderStates().stream().filter(attenderState ->
                attenderState.getProgress() == AttendingProgress.ONGOING)
            .forEach(AttenderState::mark);
        exam.setFinished(true);
    }

    private Exam getOnGoingExam(Long id) {
        Exam exam = repoHelper.findExamOrThrow(id);
        if (exam.isFinished()) {
            throw errorHelper.badRequest("Exam already finished");
        }
        if (exam.isCancelled()) {
            throw errorHelper.badRequest("Exam already cancelled");
        }
        return exam;
    }

    @Scheduled(cron = "0/10 * * * * *")
    @Transactional
    public void examFinishJob() {
        log.info(
            "\n┏################### Cron examFinishJob per 10 sec ###################┓");
        examRepository.findAll().stream().filter(Exam::isOnGoing).filter(exam ->
            exam.getExamEndTime().isBefore(LocalDateTime.now())).forEach(
            ExamService.this::finishExam
        );
        log.info(
            "\n┗########################################################################┛");
    }

}
