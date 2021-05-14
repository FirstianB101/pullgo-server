package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.mapper.ExamDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService {

    private final ExamDtoMapper dtoMapper;
    private final ExamRepository examRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public ExamService(ExamDtoMapper dtoMapper,
        ExamRepository examRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper) {
        this.dtoMapper = dtoMapper;
        this.examRepository = examRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
    }

    @Transactional
    public ExamDto.Result create(ExamDto.Create dto) {
        Exam exam = dtoMapper.asEntity(dto);

        Teacher creator = repoHelper.findTeacherOrThrow(dto.getCreatorId());
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
    public ExamDto.Result update(Long id, ExamDto.Update dto) {
        Exam entity = repoHelper.findExamOrThrow(id);
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
    public void delete(Long id) {
        Exam entity = repoHelper.findExamOrThrow(id);
        examRepository.delete(entity);
    }

    @Transactional
    public void cancelExam(Long id) {
        Exam exam = getOnGoingExam(id);

        exam.setCancelled(true);
    }

    @Transactional
    public void finishExam(Long id) {
        Exam exam = getOnGoingExam(id);

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
}
