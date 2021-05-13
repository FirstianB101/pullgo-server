package kr.pullgo.pullgoserver.service;

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
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyService extends
    BaseCrudService<Academy, Long, AcademyDto.Create, AcademyDto.Update, AcademyDto.Result> {

    private final AcademyDtoMapper dtoMapper;
    private final AcademyRepository academyRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AcademyService(AcademyDtoMapper dtoMapper,
        AcademyRepository academyRepository,
        TeacherRepository teacherRepository,
        StudentRepository studentRepository,
        ServiceErrorHelper errorHelper) {
        super(Academy.class, dtoMapper, academyRepository);
        this.dtoMapper = dtoMapper;
        this.academyRepository = academyRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.errorHelper = errorHelper;
    }

    @Override
    Academy createOnDB(AcademyDto.Create dto) {
        Academy academy = dtoMapper.asEntity(dto);

        Teacher owner = teacherRepository.findById(dto.getOwnerId())
            .orElseThrow(() -> errorHelper.notFound("Teacher id was not found"));

        academy.addTeacher(owner);
        academy.setOwner(owner);

        return academyRepository.save(academy);
    }

    @Override
    Academy updateOnDB(Academy entity, AcademyDto.Update dto) {
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
            Teacher teacher = teacherRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> errorHelper.notFound("Teacher id was not found"));

            try {
                entity.setOwner(teacher);
            } catch (TeacherNotFoundException ex) {
                throw errorHelper.badRequest("Not enrolled teacher couldn't be an owner");
            }
        }
        return academyRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        Academy academy = academyRepository.findById(id)
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

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

        return academyRepository.removeById(id);
    }

    @Transactional
    public void acceptTeacher(Long academyId, AcademyDto.AcceptTeacher dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
            .orElseThrow(() -> errorHelper.notFound("Teacher id was not found"));

        try {
            academy.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw errorHelper.badRequest("Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long academyId, AcademyDto.KickTeacher dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
            .orElseThrow(() -> errorHelper.notFound("Teacher id was not found"));

        try {
            academy.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw errorHelper.badRequest("Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long academyId, AcademyDto.AcceptStudent dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        try {
            academy.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw errorHelper.badRequest("Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long academyId, AcademyDto.KickStudent dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(() -> errorHelper.notFound("Academy id was not found"));

        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(() -> errorHelper.notFound("Student id was not found"));

        try {
            academy.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw errorHelper.badRequest("Not enrolled student");
        }
    }
}
