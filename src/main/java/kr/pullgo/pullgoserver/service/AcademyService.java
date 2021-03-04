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
import kr.pullgo.pullgoserver.util.ResponseStatusExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademyService extends
    BaseCrudService<Academy, Long, AcademyDto.Create, AcademyDto.Update, AcademyDto.Result> {

    private final AcademyDtoMapper dtoMapper;
    private final AcademyRepository academyRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AcademyService(AcademyDtoMapper dtoMapper,
        AcademyRepository academyRepository,
        TeacherRepository teacherRepository,
        StudentRepository studentRepository) {
        super(Academy.class, dtoMapper, academyRepository);
        this.dtoMapper = dtoMapper;
        this.academyRepository = academyRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    Academy createOnDB(AcademyDto.Create dto) {
        Academy academy = dtoMapper.asEntity(dto);

        Teacher teacher = teacherRepository.findById(dto.getOwnerId())
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);
        academy.setOwner(teacher);

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
                .orElseThrow(ResponseStatusExceptions::teacherNotFound);
            entity.setOwner(teacher);
        }
        return academyRepository.save(entity);
    }

    @Transactional
    public void acceptTeacher(Long academyId, AcademyDto.AcceptTeacher dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(ResponseStatusExceptions::academyNotFound);

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        try {
            academy.acceptTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied teacher");
        }
    }

    @Transactional
    public void kickTeacher(Long academyId, AcademyDto.KickTeacher dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(ResponseStatusExceptions::academyNotFound);

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
            .orElseThrow(ResponseStatusExceptions::teacherNotFound);

        try {
            academy.removeTeacher(teacher);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled teacher");
        }
    }

    @Transactional
    public void acceptStudent(Long academyId, AcademyDto.AcceptStudent dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(ResponseStatusExceptions::academyNotFound);

        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(ResponseStatusExceptions::studentNotFound);

        try {
            academy.acceptStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not applied student");
        }
    }

    @Transactional
    public void kickStudent(Long academyId, AcademyDto.KickStudent dto) {
        Academy academy = academyRepository.findById(academyId)
            .orElseThrow(ResponseStatusExceptions::academyNotFound);

        Student student = studentRepository.findById(dto.getStudentId())
            .orElseThrow(ResponseStatusExceptions::studentNotFound);

        try {
            academy.removeStudent(student);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enrolled student");
        }
    }
}
