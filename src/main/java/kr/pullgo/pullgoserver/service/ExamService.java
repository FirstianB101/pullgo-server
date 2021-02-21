package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.mapper.ExamDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExamService {

    private final ExamDtoMapper dtoMapper;
    private final ExamRepository examRepository;

    @Autowired
    public ExamService(ExamDtoMapper dtoMapper,
        ExamRepository examRepository) {
        this.dtoMapper = dtoMapper;
        this.examRepository = examRepository;
    }

    @Transactional
    public ExamDto.Result createExam(ExamDto.Create dto) {
        Exam exam = examRepository.save(dtoMapper.asEntity(dto));
        return dtoMapper.asResultDto(exam);
    }

    @Transactional
    public ExamDto.Result updateExam(Long id, ExamDto.Update dto) {
        Exam exam = examRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam id was not found"));
        if (dto.getName() != null) { exam.setName(dto.getName()); }
        if (dto.getBeginDateTime() != null) { exam.setBeginDateTime(dto.getBeginDateTime()); }
        if (dto.getEndDateTime() != null) { exam.setEndDateTime(dto.getEndDateTime()); }
        if (dto.getTimeLimit() != null) { exam.setTimeLimit(dto.getTimeLimit()); }
        if (dto.getPassScore() != null) { exam.setPassScore(dto.getPassScore()); }

        exam = examRepository.save(exam);
        return dtoMapper.asResultDto(exam);
    }

    @Transactional
    public void deleteExam(Long id) {
        int deleteResult = examRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam id was not found");
        }
    }

    @Transactional
    public ExamDto.Result getExam(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam id was not found"));
        return dtoMapper.asResultDto(exam);
    }

    @Transactional
    public List<ExamDto.Result> getExams() {
        List<Exam> exams = examRepository.findAll();
        return exams.stream()
            .map(dtoMapper::asResultDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void cancelExam(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam id was not found"));
        exam.setCancelled(true);
    }

    @Transactional
    public void finishExam(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam id was not found"));
        exam.setFinished(true);
    }
}
