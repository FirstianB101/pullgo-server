package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.dto.mapper.TeacherDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherAlreadyAppliedException;
import kr.pullgo.pullgoserver.error.exception.TeacherAlreadyEnrolledException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.authorizer.TeacherAuthorizer;
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
public class TeacherService {

    private final TeacherDtoMapper dtoMapper;
    private final AccountService accountService;
    private final TeacherRepository teacherRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final TeacherAuthorizer teacherAuthorizer;

    @Autowired
    public TeacherService(TeacherDtoMapper dtoMapper,
        AccountService accountService,
        TeacherRepository teacherRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper,
        TeacherAuthorizer teacherAuthorizer) {
        this.dtoMapper = dtoMapper;
        this.accountService = accountService;
        this.teacherRepository = teacherRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
        this.teacherAuthorizer = teacherAuthorizer;
    }

    @Transactional
    public TeacherDto.Result create(TeacherDto.Create dto) {
        Teacher teacher = dtoMapper.asEntity(dto);
        teacher.setAccount(accountService.create(dto.getAccount()));
        teacherRepository.save(teacher);
        return dtoMapper.asResultDto(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherDto.Result read(Long id) {
        Teacher entity = repoHelper.findTeacherOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<TeacherDto.Result> search(Specification<Teacher> spec, Pageable pageable) {
        Page<Teacher> entities = teacherRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public TeacherDto.Result update(Long id, TeacherDto.Update dto, Authentication authentication) {
        Teacher entity = repoHelper.findTeacherOrThrow(id);
        teacherAuthorizer.requireByOneself(authentication, entity);

        if (dto.getAccount() != null) {
            accountService.update(entity.getAccount(), dto.getAccount());
        }

        return dtoMapper.asResultDto(teacherRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Teacher teacher = repoHelper.findTeacherOrThrow(id);
        teacherAuthorizer.requireByOneself(authentication, teacher);

        teacher.getAppliedAcademies().clear();
        teacher.getAppliedClassrooms().clear();
        teacherRepository.save(teacher);

        for (Academy academy : teacher.getAcademies()) {
            academy.removeTeacher(teacher);
            academyRepository.save(academy);
        }
        for (Classroom classroom : teacher.getClassrooms()) {
            classroom.removeTeacher(teacher);
            classroomRepository.save(classroom);
        }

        teacherRepository.delete(teacher);
    }

    @Transactional
    public void applyAcademy(Long teacherId, TeacherDto.ApplyAcademy dto,
        Authentication authentication) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        teacherAuthorizer.requireByOneself(authentication, teacher);

        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());

        try {
            teacher.applyAcademy(academy);
        } catch (TeacherAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled teacher");
        } catch (TeacherAlreadyAppliedException e) {
            throw errorHelper.badRequest("Already applied teacher");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long teacherId, TeacherDto.RemoveAppliedAcademy dto,
        Authentication authentication) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());

        teacherAuthorizer.requireByOneselfOrMemberTeacher(authentication, teacher, academy);

        try {
            teacher.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw errorHelper.badRequest("Teacher did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long teacherId, TeacherDto.ApplyClassroom dto,
        Authentication authentication) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        teacherAuthorizer.requireByOneself(authentication, teacher);

        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());

        try {
            teacher.applyClassroom(classroom);
        } catch (TeacherAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled teacher");
        } catch (TeacherAlreadyAppliedException e) {
            throw errorHelper.badRequest("Already applied teacher");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long teacherId, TeacherDto.RemoveAppliedClassroom dto,
        Authentication authentication) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());

        teacherAuthorizer.requireByOneselfOrMemberTeacher(authentication, teacher, classroom);

        try {
            teacher.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw errorHelper.badRequest("Teacher did not apply classroom");
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
