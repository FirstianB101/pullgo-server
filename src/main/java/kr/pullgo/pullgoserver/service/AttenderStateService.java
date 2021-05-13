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
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttenderStateService extends
    BaseCrudService<AttenderState, Long, AttenderStateDto.Create,
        AttenderStateDto.Update, AttenderStateDto.Result> {

    private final AttenderStateRepository attenderStateRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AttenderStateService(AttenderStateDtoMapper dtoMapper,
        AttenderStateRepository attenderStateRepository,
        StudentRepository studentRepository,
        ExamRepository examRepository,
        ServiceErrorHelper errorHelper) {
        super(AttenderState.class, dtoMapper, attenderStateRepository);
        this.attenderStateRepository = attenderStateRepository;
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.errorHelper = errorHelper;
    }

    @Override
    AttenderState createOnDB(AttenderStateDto.Create dto) {
        AttenderState attenderState = AttenderState.builder().examStartTime(LocalDateTime.now())
            .build();

        Student dtoAttender = studentRepository.findById(dto.getAttenderId())
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));
        Exam dtoExam = examRepository.findById(dto.getExamId())
            .orElseThrow(() -> errorHelper.notFound("Exam id was not found"));

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
            .orElseThrow(() -> errorHelper.notFound("AttenderState id was not found"));
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
        attenderState.setProgress(AttendingProgress.COMPLETE);
    }

}
