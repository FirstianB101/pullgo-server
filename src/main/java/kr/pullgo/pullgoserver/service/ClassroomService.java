package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.dto.mapper.ClassroomDtoMapper;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassroomService {

    private final ClassroomDtoMapper dtoMapper;
    private final ClassroomRepository classroomRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public ClassroomService(ClassroomDtoMapper dtoMapper,
        ClassroomRepository classroomRepository,
        TeacherRepository teacherRepository,
        StudentRepository studentRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper) {
        this.dtoMapper = dtoMapper;
        this.classroomRepository = classroomRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
    }

    @Transactional
    public ClassroomDto.Result create(ClassroomDto.Create dto) {
        Classroom classroom = dtoMapper.asEntity(dto);

        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());
        academy.addClassroom(classroom);

        Teacher creator = repoHelper.findTeacherOrThrow(dto.getCreatorId());
        classroom.addTeacher(creator);

        return dtoMapper.asResultDto(classroomRepository.save(classroom));
    }

    @Transactional(readOnly = true)
    public ClassroomDto.Result read(Long id) {
        Classroom entity = repoHelper.findClassroomOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<ClassroomDto.Result> search(Specification<Classroom> spec, Pageable pageable) {
        Page<Classroom> entities = classroomRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public ClassroomDto.Result update(Long id, ClassroomDto.Update dto) {
        Classroom entity = repoHelper.findClassroomOrThrow(id);
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        return dtoMapper.asResultDto(classroomRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Classroom classroom = repoHelper.findClassroomOrThrow(id);

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

        classroomRepository.delete(classroom);
    }

    @Transactional
    public void acceptTeacher(Long classroomId, ClassroomDto.AcceptTeacher dto) {
        Classroom classroom = repoHelper.findClassroomOrThrow(classroomId);
        Teacher teacher = repoHelper.findTeacherOrThrow(dto.getTeacherId());

        try {
            classroom.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw errorHelper.badRequest("Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long classroomId, ClassroomDto.KickTeacher dto) {
        Classroom classroom = repoHelper.findClassroomOrThrow(classroomId);
        Teacher teacher = repoHelper.findTeacherOrThrow(dto.getTeacherId());

        try {
            classroom.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw errorHelper.badRequest("Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long classroomId, ClassroomDto.AcceptStudent dto) {
        Classroom classroom = repoHelper.findClassroomOrThrow(classroomId);
        Student student = repoHelper.findStudentOrThrow(dto.getStudentId());

        try {
            classroom.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw errorHelper.badRequest("Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long classroomId, ClassroomDto.KickStudent dto) {
        Classroom classroom = repoHelper.findClassroomOrThrow(classroomId);
        Student student = repoHelper.findStudentOrThrow(dto.getStudentId());

        try {
            classroom.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw errorHelper.badRequest("Not enrolled student");
        }
    }
}
