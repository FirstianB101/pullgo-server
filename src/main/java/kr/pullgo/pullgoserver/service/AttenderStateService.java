package kr.pullgo.pullgoserver.service;

import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.dto.mapper.AttenderStateDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.util.ResponseStatusExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttenderStateService extends
    BaseCrudService<AttenderState, Long, AttenderStateDto.Create,
        AttenderStateDto.Update, AttenderStateDto.Result> {

    private final AttenderStateRepository attenderStateRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;

    @Autowired
    public AttenderStateService(AttenderStateDtoMapper dtoMapper,
        AttenderStateRepository attenderStateRepository,
        StudentRepository studentRepository,
        ExamRepository examRepository) {
        super(AttenderState.class, dtoMapper, attenderStateRepository);
        this.attenderStateRepository = attenderStateRepository;
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
    }

    @Override
    AttenderState createOnDB(AttenderStateDto.Create dto) {
        AttenderState attenderState = AttenderState.builder().examStartTime(LocalDateTime.now())
            .build();

        Student dtoAttender = studentRepository.findById(dto.getAttenderId())
            .orElseThrow(ResponseStatusExceptions::studentNotFound);
        Exam dtoExam = examRepository.findById(dto.getExamId())
            .orElseThrow(ResponseStatusExceptions::examNotFound);

        attenderState.setAttender(dtoAttender);
        attenderState.setExam(dtoExam);

        return attenderStateRepository.save(attenderState);
    }

    @Override
    AttenderState updateOnDB(AttenderState entity, AttenderStateDto.Update dto) {
        if (dto.getProgress() != null) {
            entity.setProgress(dto.getProgress());
        }
        if (dto.getScore() != null) {
            entity.setScore(dto.getScore());
        }
        return attenderStateRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        return attenderStateRepository.removeById(id);
    }

    @Transactional
    public void submit(Long id) {
        AttenderState attenderState = attenderStateRepository.findById(id)
            .orElseThrow(ResponseStatusExceptions::attenderStateNotFound);
        Exam exam = attenderState.getExam();
        AttendingProgress presentAttendingProgress = attenderState.getProgress();

        if (presentAttendingProgress != AttendingProgress.ONGOING) {
            String reason = "attender state already " + presentAttendingProgress.toString();
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, reason);
        }
        if (attenderState.isAfterTimeLimit(attenderState.getExamStartTime())) {
            throw ResponseStatusExceptions.attenderStateSubmittedAfterTimeLimit();
        }
        if (exam.isFinished()) {
            throw ResponseStatusExceptions.attenderStateSubmittedAlreadyFinishedExam();
        }
        if (exam.isCancelled()) {
            throw ResponseStatusExceptions.attenderStateSubmittedAlreadyCancelledExam();
        }
        if (attenderState.isOutOfTimeRange(attenderState.getExamStartTime())) {
            throw ResponseStatusExceptions.attenderStateSubmittedAfterTimeRange();
        }
        attenderState.setProgress(AttendingProgress.COMPLETE);
    }

}
