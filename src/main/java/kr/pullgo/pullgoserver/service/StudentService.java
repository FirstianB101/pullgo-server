package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;

    @Autowired
    public StudentService(
        StudentRepository studentRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository
    ) {
        this.studentRepository = studentRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public StudentDto.Result createStudent(StudentDto.Create dto) {
        Student student = studentRepository.save(StudentDto.mapToEntity(dto));
        return StudentDto.mapFromEntity(student);
    }

    @Transactional
    public StudentDto.Result updateStudent(Long id, StudentDto.Update dto) {
        Student student = studentRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        AccountDto.Update dtoAccount = dto.getAccount();
        Account entityAccount = student.getAccount();
        if (dtoAccount != null) {
            if (dtoAccount.getPassword() != null) {
                entityAccount.setPassword(dtoAccount.getPassword());
            }
            if (dtoAccount.getFullName() != null) {
                entityAccount.setFullName(dtoAccount.getFullName());
            }
            if (dtoAccount.getPhone() != null) { entityAccount.setPhone(dtoAccount.getPhone()); }
        }
        if (dto.getParentPhone() != null) { student.setParentPhone(dto.getParentPhone()); }
        if (dto.getSchoolName() != null) { student.setSchoolName(dto.getSchoolName()); }
        if (dto.getSchoolYear() != null) { student.setSchoolYear(dto.getSchoolYear()); }

        student = studentRepository.save(student);
        return StudentDto.mapFromEntity(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        int deleteResult = studentRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found");
        }
    }

    @Transactional
    public StudentDto.Result getStudent(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));
        return StudentDto.mapFromEntity(student);
    }

    @Transactional
    public List<StudentDto.Result> getStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
            .map(StudentDto::mapFromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public void applyAcademy(Long studentId, StudentDto.ApplyAcademy dto) {

        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        Academy academy = academyRepository.findById(dto.getAcademyId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        try {
            student.applyAcademy(academy);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled student");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long studentId, StudentDto.RemoveAppliedAcademy dto) {

        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        Academy academy = academyRepository.findById(dto.getAcademyId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        try {
            student.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Student did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long studentId, StudentDto.ApplyClassroom dto) {

        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        Classroom classroom = classroomRepository.findById(dto.getClassroomId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        try {
            student.applyClassroom(classroom);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled student");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long studentId, StudentDto.RemoveAppliedClassroom dto) {

        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        Classroom classroom = classroomRepository.findById(dto.getClassroomId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        try {
            student.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Student did not apply classroom");
        }
    }

}
