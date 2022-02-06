package kr.pullgo.pullgoserver.service.exam;

import java.util.List;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.mapper.ExamDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService {

    private final ExamDtoMapper dtoMapper;
    private final ExamRepository examRepository;
    private final RepositoryHelper repoHelper;
    private final ExamAuthorizer examAuthorizer;
    private final ExamManagement manageMent;

    @Autowired
    public ExamService(ExamDtoMapper dtoMapper,
        ExamRepository examRepository,
        RepositoryHelper repoHelper,
        ExamAuthorizer examAuthorizer,
        ExamManagement manageMent) {
        this.dtoMapper = dtoMapper;
        this.examRepository = examRepository;
        this.repoHelper = repoHelper;
        this.examAuthorizer = examAuthorizer;
        this.manageMent = manageMent;
    }

    @Transactional
    public ExamDto.Result create(ExamDto.Create dto, Authentication authentication) {
        Exam exam = dtoMapper.asEntity(dto);

        Teacher creator = repoHelper.findTeacherOrThrow(dto.getCreatorId());
        examAuthorizer.requireByOneself(authentication, creator);
        exam.setCreator(creator);

        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());
        classroom.addExam(exam);

        exam = examRepository.save(exam);

        manageMent.registerCronJob(exam);

        return dtoMapper.asResultDto(exam);
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
        manageMent.removeCronJob(entity);
        manageMent.registerCronJob(entity);

        return dtoMapper.asResultDto(examRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Exam entity = repoHelper.findExamOrThrow(id);
        examAuthorizer.requireCreator(authentication, entity);

        manageMent.removeCronJob(entity);
        examRepository.delete(entity);
    }
}