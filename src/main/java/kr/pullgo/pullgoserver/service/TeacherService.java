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
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherService extends
    BaseCrudService<Teacher, Long, TeacherDto.Create, TeacherDto.Update, TeacherDto.Result> {

    private final TeacherDtoMapper dtoMapper;
    private final TeacherRepository teacherRepository;
    private final AcademyRepository academyRepository;
    private final ClassroomRepository classroomRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public TeacherService(TeacherDtoMapper dtoMapper,
        TeacherRepository teacherRepository,
        AcademyRepository academyRepository,
        ClassroomRepository classroomRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper) {
        super(Teacher.class, dtoMapper, teacherRepository);
        this.dtoMapper = dtoMapper;
        this.teacherRepository = teacherRepository;
        this.academyRepository = academyRepository;
        this.classroomRepository = classroomRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
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
        Teacher teacher = repoHelper.findTeacherOrThrow(id);

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

        return teacherRepository.removeById(id);
    }

    @Transactional
    public void applyAcademy(Long teacherId, TeacherDto.ApplyAcademy dto) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());

        try {
            teacher.applyAcademy(academy);
        } catch (TeacherAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled teacher");
        }
    }

    @Transactional
    public void removeAppliedAcademy(Long teacherId, TeacherDto.RemoveAppliedAcademy dto) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        Academy academy = repoHelper.findAcademyOrThrow(dto.getAcademyId());

        try {
            teacher.removeAppliedAcademy(academy);
        } catch (AcademyNotFoundException e) {
            throw errorHelper.badRequest("Teacher did not apply academy");
        }
    }

    @Transactional
    public void applyClassroom(Long teacherId, TeacherDto.ApplyClassroom dto) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());

        try {
            teacher.applyClassroom(classroom);
        } catch (TeacherAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled teacher");
        }
    }

    @Transactional
    public void removeAppliedClassroom(Long teacherId, TeacherDto.RemoveAppliedClassroom dto) {
        Teacher teacher = repoHelper.findTeacherOrThrow(teacherId);
        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());

        try {
            teacher.removeAppliedClassroom(classroom);
        } catch (ClassroomNotFoundException e) {
            throw errorHelper.badRequest("Teacher did not apply classroom");
        }
    }
}
