package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.dto.mapper.TeacherDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeacherService {

    private final TeacherDtoMapper dtoMapper;
    private final TeacherRepository teacherRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;

    @Autowired
    public TeacherService(TeacherDtoMapper dtoMapper,
        TeacherRepository teacherRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository) {
        this.dtoMapper = dtoMapper;
        this.teacherRepository = teacherRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public TeacherDto.Result createTeacher(TeacherDto.Create dto) {
        Teacher teacher = teacherRepository.save(dtoMapper.asEntity(dto));
        return dtoMapper.asResultDto(teacher);
    }

    @Transactional
    public TeacherDto.Result updateTeacher(Long id, TeacherDto.Update dto) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        AccountDto.Update dtoAccount = dto.getAccount();
        Account entityAccount = teacher.getAccount();
        if (dtoAccount != null) {
            if (dtoAccount.getPassword() != null) {
                entityAccount.setPassword(dtoAccount.getPassword());
            }
            if (dtoAccount.getFullName() != null) {
                entityAccount.setFullName(dtoAccount.getFullName());
            }
            if (dtoAccount.getPhone() != null) { entityAccount.setPhone(dtoAccount.getPhone()); }
        }
        teacher = teacherRepository.save(teacher);
        return dtoMapper.asResultDto(teacher);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        int deleteResult = teacherRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found");
        }
    }

    @Transactional
    public TeacherDto.Result getTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));
        return dtoMapper.asResultDto(teacher);
    }

    @Transactional
    public List<TeacherDto.Result> getTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();
        return teachers.stream()
            .map(dtoMapper::asResultDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void applyAcademy(Long teacherId, TeacherDto.ApplyAcademy dto) {

        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        Academy academy = academyRepository.findById(dto.getAcademyId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        try {
            teacher.applyAcademy(academy);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled teacher");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long teacherId, TeacherDto.RemoveAppliedAcademy dto) {

        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        Academy academy = academyRepository.findById(dto.getAcademyId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        try {
            teacher.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Teacher did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long teacherId, TeacherDto.ApplyClassroom dto) {

        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        Classroom classroom = classroomRepository.findById(dto.getClassroomId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        try {
            teacher.applyClassroom(classroom);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled teacher");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long teacherId, TeacherDto.RemoveAppliedClassroom dto) {

        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        Classroom classroom = classroomRepository.findById(dto.getClassroomId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        try {
            teacher.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Teacher did not apply classroom");
        }
    }
}
