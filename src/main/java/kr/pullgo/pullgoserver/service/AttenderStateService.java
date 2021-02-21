package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.dto.AttenderStateDto.Result;
import kr.pullgo.pullgoserver.dto.mapper.AttenderStateDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttenderStateService {

    private final AttenderStateDtoMapper dtoMapper;
    private final AttenderStateRepository attenderStateRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;

    @Autowired
    public AttenderStateService(AttenderStateDtoMapper dtoMapper,
        AttenderStateRepository attenderStateRepository,
        StudentRepository studentRepository,
        ExamRepository examRepository) {
        this.dtoMapper = dtoMapper;
        this.attenderStateRepository = attenderStateRepository;
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
    }

    @Transactional
    public AttenderStateDto.Result createAttenderState(AttenderStateDto.Create dto) {
        AttenderState attenderState = new AttenderState();
        Student dtoAttender = studentRepository.findById(dto.getAttenderId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Student id was not found"));
        Exam dtoExam = examRepository.findById(dto.getExamId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam id was not found"));

        attenderState.setAttender(dtoAttender);
        attenderState.setExam(dtoExam);

        attenderState = attenderStateRepository.save(attenderState);
        return dtoMapper.asResultDto(attenderState);
    }

    @Transactional
    public AttenderStateDto.Result updateAttenderState(Long id, AttenderStateDto.Update dto) {
        AttenderState attenderState = attenderStateRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderState id was not found"));

        if (dto.getProgress() != null) { attenderState.setProgress(dto.getProgress()); }
        if (dto.getScore() != null) { attenderState.setScore(dto.getScore()); }

        attenderState = attenderStateRepository.save(attenderState);
        return dtoMapper.asResultDto(attenderState);
    }

    @Transactional
    public void deleteAttenderState(Long id) {
        int deleteResult = attenderStateRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderState id was not found");
        }
    }

    @Transactional
    public AttenderStateDto.Result getAttenderState(Long id) {
        AttenderState attenderState = attenderStateRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderState id was not found"));
        return dtoMapper.asResultDto(attenderState);
    }

    @Transactional
    public List<Result> getAttenderStates() {
        List<AttenderState> attenderStates = attenderStateRepository.findAll();
        return attenderStates.stream()
            .map(dtoMapper::asResultDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void submitAttenderState(Long id) {
        AttenderState attenderState = attenderStateRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderState id was not found"));
        attenderState.setSubmitted(true);
    }
}
