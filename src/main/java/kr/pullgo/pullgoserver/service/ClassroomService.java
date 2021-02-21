package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.dto.mapper.ClassroomDtoMapper;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClassroomService {

    private final ClassroomDtoMapper dtoMapper;
    private final ClassroomRepository classroomRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final AcademyRepository academyRepository;

    @Autowired
    public ClassroomService(ClassroomDtoMapper dtoMapper,
        ClassroomRepository classroomRepository,
        TeacherRepository teacherRepository,
        StudentRepository studentRepository,
        AcademyRepository academyRepository) {
        this.dtoMapper = dtoMapper;
        this.classroomRepository = classroomRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.academyRepository = academyRepository;
    }

    @Transactional
    public ClassroomDto.Result createClassroom(ClassroomDto.Create dto) {
        Classroom classroom = dtoMapper.asEntity(dto);
        Academy academy = academyRepository.findById(dto.getAcademyId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academy id was not found"));
        academy.addClassroom(classroom);
        classroom = classroomRepository.save(classroom);
        return dtoMapper.asResultDto(classroom);
    }

    @Transactional
    public ClassroomDto.Result updateClassroom(Long id, ClassroomDto.Update dto) {
        Classroom classroom = classroomRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));
        if (dto.getName() != null) { classroom.setName(dto.getName()); }

        classroom = classroomRepository.save(classroom);
        return dtoMapper.asResultDto(classroom);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        int deleteResult = classroomRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found");
        }
    }

    @Transactional
    public ClassroomDto.Result getClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));
        return dtoMapper.asResultDto(classroom);
    }

    @Transactional
    public List<ClassroomDto.Result> getClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        return classrooms.stream()
            .map(dtoMapper::asResultDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void acceptTeacher(Long classroomId, ClassroomDto.AcceptTeacher dto) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        try {
            classroom.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long classroomId, ClassroomDto.KickTeacher dto) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher id was not found"));

        try {
            classroom.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long classroomId, ClassroomDto.AcceptStudent dto) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        Student student = studentRepository.findById(dto.getStudentId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        try {
            classroom.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long classroomId, ClassroomDto.KickStudent dto) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom id was not found"));

        Student student = studentRepository.findById(dto.getStudentId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student id was not found"));

        try {
            classroom.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled student");
        }
    }
}
