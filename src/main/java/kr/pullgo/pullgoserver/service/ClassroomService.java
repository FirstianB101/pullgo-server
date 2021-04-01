package kr.pullgo.pullgoserver.service;

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
import kr.pullgo.pullgoserver.util.ResponseStatusExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClassroomService extends
    BaseCrudService<Classroom, Long, ClassroomDto.Create,
        ClassroomDto.Update, ClassroomDto.Result> {

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
        super(Classroom.class, dtoMapper, classroomRepository);
        this.dtoMapper = dtoMapper;
        this.classroomRepository = classroomRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.academyRepository = academyRepository;
    }

    @Override
    Classroom createOnDB(ClassroomDto.Create dto) {
        Classroom classroom = dtoMapper.asEntity(dto);

        Academy academy = academyRepository.findById(dto.getAcademyId())
            .orElseThrow(ResponseStatusExceptions::academyNotFound);
        academy.addClassroom(classroom);

        Teacher creator = teacherRepository.findById(dto.getCreatorId())
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);
        classroom.addTeacher(creator);

        return classroomRepository.save(classroom);
    }

    @Override
    Classroom updateOnDB(Classroom entity, ClassroomDto.Update dto) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        return classroomRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        classroom.getStudents().clear();
        classroom.getTeachers().clear();
        classroomRepository.save(classroom);

        for (Student student : classroom.getApplyingStudents()) {
            student.removeAppliedClassroom(classroom);
            studentRepository.save(student);
        }
        for (Teacher teacher : classroom.getApplyingTeachers()) {
            teacher.removeAppliedClassroom(classroom);
            teacherRepository.save(teacher);
        }

        return classroomRepository.removeById(id);
    }

    @Transactional
    public void acceptTeacher(Long classroomId, ClassroomDto.AcceptTeacher dto) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        try {
            classroom.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long classroomId, ClassroomDto.KickTeacher dto) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        try {
            classroom.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long classroomId, ClassroomDto.AcceptStudent dto) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(ResponseStatusExceptions::studentNotFound);

        try {
            classroom.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long classroomId, ClassroomDto.KickStudent dto) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);

        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(ResponseStatusExceptions::studentNotFound);

        try {
            classroom.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled student");
        }
    }
}
