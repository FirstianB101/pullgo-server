package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.dto.mapper.StudentDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.StudentAlreadyEnrolledException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.service.authorizer.StudentAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentDtoMapper dtoMapper;
    private final AccountService accountService;
    private final StudentRepository studentRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final StudentAuthorizer studentAuthorizer;

    @Autowired
    public StudentService(StudentDtoMapper dtoMapper,
        AccountService accountService,
        StudentRepository studentRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper,
        StudentAuthorizer studentAuthorizer) {
        this.dtoMapper = dtoMapper;
        this.accountService = accountService;
        this.studentRepository = studentRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
        this.studentAuthorizer = studentAuthorizer;
    }

    @Transactional
    public StudentDto.Result create(StudentDto.Create dto) {
        Student student = dtoMapper.asEntity(dto);
        student.setAccount(accountService.create(dto.getAccount()));
        studentRepository.save(student);
        return dtoMapper.asResultDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto.Result read(Long id) {
        Student entity = repoHelper.findStudentOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<StudentDto.Result> search(Specification<Student> spec, Pageable pageable) {
        Page<Student> entities = studentRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public StudentDto.Result update(Long id, StudentDto.Update dto, Authentication authentication) {
        Student entity = repoHelper.findStudentOrThrow(id);

        studentAuthorizer.requireByOneself(authentication, entity);

        if (dto.getParentPhone() != null) {
            entity.setParentPhone(dto.getParentPhone());
        }
        if (dto.getSchoolName() != null) {
            entity.setSchoolName(dto.getSchoolName());
        }
        if (dto.getSchoolYear() != null) {
            entity.setSchoolYear(dto.getSchoolYear());
        }
        if (dto.getAccount() != null) {
            accountService.update(entity.getAccount(), dto.getAccount());
        }

        return dtoMapper.asResultDto(studentRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Student student = repoHelper.findStudentOrThrow(id);
        studentAuthorizer.requireByOneself(authentication, student);

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

        studentRepository.delete(student);
    }

    @Transactional
    public void applyAcademy(Long studentId, StudentDto.ApplyAcademy dto,
        Authentication authentication) {
        Student student = repoHelper.findStudentOrThrow(studentId);

        studentAuthorizer.requireByOneself(authentication, student);

        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());

        try {
            student.applyAcademy(academy);
        } catch (StudentAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled student");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long studentId, StudentDto.RemoveAppliedAcademy dto,
        Authentication authentication) {
        Student student = repoHelper.findStudentOrThrow(studentId);
        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());

        studentAuthorizer.requireByOneselfOrMemberTeacher(authentication, student, academy);

        try {
            student.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw errorHelper.badRequest("Student did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long studentId, StudentDto.ApplyClassroom dto,
        Authentication authentication) {
        Student student = repoHelper.findStudentOrThrow(studentId);

        studentAuthorizer.requireByOneself(authentication, student);

        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());

        try {
            student.applyClassroom(classroom);
        } catch (StudentAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled student");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long studentId, StudentDto.RemoveAppliedClassroom dto,
        Authentication authentication) {
        Student student = repoHelper.findStudentOrThrow(studentId);
        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());

        studentAuthorizer.requireByOneselfOrMemberTeacher(authentication, student, classroom);

        try {
            student.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw errorHelper.badRequest("Student did not apply classroom");
        }
    }

    @Transactional
    public AccountDto.CheckDuplicationResult checkDuplicateUsername(String username) {
        Boolean result = accountService.checkDuplicateUsername(username);
        return AccountDto.CheckDuplicationResult.builder()
            .exists(result)
            .build();
    }
}
