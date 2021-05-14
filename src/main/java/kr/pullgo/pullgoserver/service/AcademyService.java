package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.dto.mapper.AcademyDtoMapper;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
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
public class AcademyService {

    private final AcademyDtoMapper dtoMapper;
    private final AcademyRepository academyRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AcademyService(AcademyDtoMapper dtoMapper,
        AcademyRepository academyRepository,
        TeacherRepository teacherRepository,
        StudentRepository studentRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper) {
        this.dtoMapper = dtoMapper;
        this.academyRepository = academyRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
    }

    @Transactional
    public AcademyDto.Result create(AcademyDto.Create dto) {
        Academy academy = dtoMapper.asEntity(dto);

        Teacher owner = repoHelper.findTeacherOrThrow(dto.getOwnerId());
        academy.addTeacher(owner);
        academy.setOwner(owner);

        return dtoMapper.asResultDto(academyRepository.save(academy));
    }

    @Transactional(readOnly = true)
    public AcademyDto.Result read(Long id) {
        Academy entity = repoHelper.findAcademyOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AcademyDto.Result> search(Specification<Academy> spec, Pageable pageable) {
        Page<Academy> entities = academyRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public AcademyDto.Result update(Long id, AcademyDto.Update dto) {
        Academy entity = repoHelper.findAcademyOrThrow(id);
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            entity.setAddress(dto.getAddress());
        }
        if (dto.getOwnerId() != null) {
            Teacher teacher = repoHelper.findTeacherOrThrow(dto.getOwnerId());

            try {
                entity.setOwner(teacher);
            } catch (TeacherNotFoundException ex) {
                throw errorHelper.badRequest("Not enrolled teacher couldn't be an owner");
            }
        }
        return dtoMapper.asResultDto(academyRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Academy academy = repoHelper.findAcademyOrThrow(id);

        academy.getStudents().clear();
        academy.getTeachers().clear();
        academyRepository.save(academy);

        for (Student student : academy.getApplyingStudents()) {
            student.removeAppliedAcademy(academy);
            studentRepository.save(student);
        }
        for (Teacher teacher : academy.getApplyingTeachers()) {
            teacher.removeAppliedAcademy(academy);
            teacherRepository.save(teacher);
        }

        academyRepository.delete(academy);
    }

    @Transactional
    public void acceptTeacher(Long academyId, AcademyDto.AcceptTeacher dto) {
        Academy academy = repoHelper.findAcademyOrThrow(academyId);
        Teacher teacher = repoHelper.findTeacherOrThrow(dto.getTeacherId());

        try {
            academy.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw errorHelper.badRequest("Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long academyId, AcademyDto.KickTeacher dto) {
        Academy academy = repoHelper.findAcademyOrThrow(academyId);
        Teacher teacher = repoHelper.findTeacherOrThrow(dto.getTeacherId());

        try {
            academy.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw errorHelper.badRequest("Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long academyId, AcademyDto.AcceptStudent dto) {
        Academy academy = repoHelper.findAcademyOrThrow(academyId);
        Student student = repoHelper.findStudentOrThrow(dto.getStudentId());

        try {
            academy.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw errorHelper.badRequest("Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long academyId, AcademyDto.KickStudent dto) {
        Academy academy = repoHelper.findAcademyOrThrow(academyId);
        Student student = repoHelper.findStudentOrThrow(dto.getStudentId());

        try {
            academy.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw errorHelper.badRequest("Not enrolled student");
        }
    }
}
