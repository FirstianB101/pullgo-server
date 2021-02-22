package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.mapper.ExamDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.util.ResponseStatusExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService extends
    BaseCrudService<Exam, Long, ExamDto.Create, ExamDto.Update, ExamDto.Result> {

    private final ExamDtoMapper dtoMapper;
    private final ExamRepository examRepository;

    @Autowired
    public ExamService(ExamDtoMapper dtoMapper,
        ExamRepository examRepository) {
        super(Exam.class, dtoMapper, examRepository);
        this.dtoMapper = dtoMapper;
        this.examRepository = examRepository;
    }

    @Override
    Exam createOnDB(ExamDto.Create dto) {
        return examRepository.save(dtoMapper.asEntity(dto));
    }

    @Override
    Exam updateOnDB(Exam entity, ExamDto.Update dto) {
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
        return examRepository.save(entity);
    }

    @Transactional
    public void cancelExam(Long id) {
        Exam exam = examRepository.findById(id)
            .orElseThrow(ResponseStatusExceptions::examNotFound);
        exam.setCancelled(true);
    }

    @Transactional
    public void finishExam(Long id) {
        Exam exam = examRepository.findById(id)
            .orElseThrow(ResponseStatusExceptions::examNotFound);
        exam.setFinished(true);
    }
}
