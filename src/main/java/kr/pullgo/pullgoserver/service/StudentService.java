package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.dto.mapper.StudentDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.StudentAlreadyEnrolledException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService extends
    BaseCrudService<Student, Long, StudentDto.Create, StudentDto.Update, StudentDto.Result> {

    private final StudentDtoMapper dtoMapper;
    private final StudentRepository studentRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public StudentService(StudentDtoMapper dtoMapper,
        StudentRepository studentRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository,
        ServiceErrorHelper errorHelper) {
        super(Student.class, dtoMapper, studentRepository);
        this.dtoMapper = dtoMapper;
        this.studentRepository = studentRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
        this.errorHelper = errorHelper;
    }

    @Override
    Student createOnDB(StudentDto.Create dto) {
        return studentRepository.save(dtoMapper.asEntity(dto));
    }

    @Override
    Student updateOnDB(Student entity, StudentDto.Update dto) {
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
        if (dto.getParentPhone() != null) {
            entity.setParentPhone(dto.getParentPhone());
        }
        if (dto.getSchoolName() != null) {
            entity.setSchoolName(dto.getSchoolName());
        }
        if (dto.getSchoolYear() != null) {
            entity.setSchoolYear(dto.getSchoolYear());
        }
        return studentRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        student.getAppliedAcademies().clear();
        student.getAppliedClassrooms().clear();
        studentRepository.save(student);

        for (Academy academy : student.getAcademies()) {
            academy.removeStudent(student);
            academyRepository.save(academy);
        }
        for (Classroom classroom : student.getClassrooms()) {
            classroom.removeStudent(student);
            classroomRepository.save(classroom);
        }

        return studentRepository.removeById(id);
    }

    @Transactional
    public void applyAcademy(Long studentId, StudentDto.ApplyAcademy dto) {

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        Academy academy = academyRepository.findById(dto.getAcademyId())
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

        try {
            student.applyAcademy(academy);
        } catch (StudentAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled student");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long studentId, StudentDto.RemoveAppliedAcademy dto) {

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        Academy academy = academyRepository.findById(dto.getAcademyId())
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

        try {
            student.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw errorHelper.badRequest("Student did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long studentId, StudentDto.ApplyClassroom dto) {

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
            .orElseThrow(() -> errorHelper.notFound("Classroom id was not found"));

        try {
            student.applyClassroom(classroom);
        } catch (StudentAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled student");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long studentId, StudentDto.RemoveAppliedClassroom dto) {

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
            .orElseThrow(() -> errorHelper.notFound("Classroom id was not found"));

        try {
            student.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw errorHelper.badRequest("Student did not apply classroom");
        }
    }

}
