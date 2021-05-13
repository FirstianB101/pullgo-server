package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.mapper.ExamDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService extends
    BaseCrudService<Exam, Long, ExamDto.Create, ExamDto.Update, ExamDto.Result> {

    private final ExamDtoMapper dtoMapper;
    private final ExamRepository examRepository;
    private final ClassroomRepository classroomRepository;
    private final TeacherRepository teacherRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public ExamService(ExamDtoMapper dtoMapper,
        ExamRepository examRepository,
        ClassroomRepository classroomRepository,
        TeacherRepository teacherRepository,
        ServiceErrorHelper errorHelper) {
        super(Exam.class, dtoMapper, examRepository);
        this.dtoMapper = dtoMapper;
        this.examRepository = examRepository;
        this.classroomRepository = classroomRepository;
        this.teacherRepository = teacherRepository;
        this.errorHelper = errorHelper;
    }

    @Override
    Exam createOnDB(ExamDto.Create dto) {
        Exam exam = dtoMapper.asEntity(dto);
        Teacher creator = teacherRepository.findById(dto.getCreatorId())
            .orElseThrow(() -> errorHelper.notFound("Teacher id was not found"));
        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
            .orElseThrow(() -> errorHelper.notFound("Classroom id was not found"));
        classroom.addExam(exam);
        exam.setCreator(creator);
        return examRepository.save(exam);
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

    @Override
    int removeOnDB(Long id) {
        return examRepository.removeById(id);
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
        Exam exam = examRepository.findById(id)
            .orElseThrow(() -> errorHelper.notFound("Exam id was not found"));
        if (exam.isFinished()) {
            throw errorHelper.badRequest("Exam already finished");
        }
        if (exam.isCancelled()) {
            throw errorHelper.badRequest("Exam already cancelled");
        }
        return exam;
    }
}
