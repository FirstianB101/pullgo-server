package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademyService {

    private final AcademyRepository academyRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AcademyService(
        AcademyRepository academyRepository,
        TeacherRepository teacherRepository,
        StudentRepository studentRepository
    ) {
        this.academyRepository = academyRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public AcademyDto.Result createAcademy(AcademyDto.Create dto) {
        Academy academy = academyRepository.save(AcademyDto.mapToEntity(dto));
        return AcademyDto.mapFromEntity(academy);
    }

    @Transactional
    public AcademyDto.Result updateAcademy(Long id, AcademyDto.Update dto) {
        Academy academy = academyRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));
        if (dto.getName() != null) { academy.setName(dto.getName()); }
        if (dto.getPhone() != null) { academy.setPhone(dto.getPhone()); }
        if (dto.getAddress() != null) { academy.setAddress(dto.getAddress()); }
        academy = academyRepository.save(academy);
        return AcademyDto.mapFromEntity(academy);
    }

    @Transactional
    public void deleteAcademy(Long id) {
        int deleteResult = academyRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found");
        }
    }

    @Transactional
    public AcademyDto.Result getAcademy(Long id) {
        Academy academy = academyRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));
        return AcademyDto.mapFromEntity(academy);
    }

    @Transactional
    public List<AcademyDto.Result> getAcademies() {
        List<Academy> academies = academyRepository.findAll();
        return academies.stream()
            .map(AcademyDto::mapFromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public void acceptTeacher(Long academyId, Long teacherId) {
        Academy academy = academyRepository.findById(academyId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        try {
            academy.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long academyId, Long teacherId) {
        Academy academy = academyRepository.findById(academyId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        try {
            academy.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long academyId, Long studentId) {
        Academy academy = academyRepository.findById(academyId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        try {
            academy.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long academyId, Long studentId) {
        Academy academy = academyRepository.findById(academyId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));

        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        try {
            academy.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled student");
        }
    }
}
