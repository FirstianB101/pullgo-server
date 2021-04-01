package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.dto.mapper.TeacherDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherAlreadyEnrolledException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.util.ResponseStatusExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeacherService extends
    BaseCrudService<Teacher, Long, TeacherDto.Create, TeacherDto.Update, TeacherDto.Result> {

    private final TeacherDtoMapper dtoMapper;
    private final TeacherRepository teacherRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;

    @Autowired
    public TeacherService(TeacherDtoMapper dtoMapper,
        TeacherRepository teacherRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository) {
        super(Teacher.class, dtoMapper, teacherRepository);
        this.dtoMapper = dtoMapper;
        this.teacherRepository = teacherRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
    }

    @Override
    Teacher createOnDB(TeacherDto.Create dto) {
        return teacherRepository.save(dtoMapper.asEntity(dto));
    }

    @Override
    Teacher updateOnDB(Teacher entity, TeacherDto.Update dto) {
        AccountDto.Update dtoAccount = dto.getAccount();
        Account entityAccount = entity.getAccount();
        if (dtoAccount != null) {
            if (dtoAccount.getPassword() != null) {
                entityAccount.setPassword(dtoAccount.getPassword());
            }
            if (dtoAccount.getFullName() != null) {
                entityAccount.setFullName(dtoAccount.getFullName());
            }
            if (dtoAccount.getPhone() != null) {
                entityAccount.setPhone(dtoAccount.getPhone());
            }
        }
        return teacherRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        return teacherRepository.removeById(id);
    }

    @Transactional
    public void applyAcademy(Long teacherId, TeacherDto.ApplyAcademy dto) {

        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        Academy academy = academyRepository.findById(dto.getAcademyId())
            .orElseThrow(ResponseStatusExceptions::academyNotFound);

        try {
            teacher.applyAcademy(academy);
        } catch (TeacherAlreadyEnrolledException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already enrolled teacher");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long teacherId, TeacherDto.RemoveAppliedAcademy dto) {

        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        Academy academy = academyRepository.findById(dto.getAcademyId())
            .orElseThrow(ResponseStatusExceptions::academyNotFound);

        try {
            teacher.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Teacher did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long teacherId, TeacherDto.ApplyClassroom dto) {

        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        try {
            teacher.applyClassroom(classroom);
        } catch (TeacherAlreadyEnrolledException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already enrolled teacher");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long teacherId, TeacherDto.RemoveAppliedClassroom dto) {

        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        try {
            teacher.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Teacher did not apply classroom");
        }
    }
}
