package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.persistence.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentDtoMapper implements DtoMapper<Student, StudentDto.Create, StudentDto.Result> {

    private final AccountDtoMapper accountDtoMapper;

    @Autowired
    public StudentDtoMapper(AccountDtoMapper accountDtoMapper) {
        this.accountDtoMapper = accountDtoMapper;
    }

    @Override
    public Student asEntity(StudentDto.Create dto) {
        Student student = Student.builder()
            .parentPhone(dto.getParentPhone())
            .schoolName(dto.getSchoolName())
            .schoolYear(dto.getSchoolYear())
            .build();
        student.setAccount(accountDtoMapper.asEntity(dto.getAccount()));
        return student;
    }

    @Override
    public StudentDto.Result asResultDto(Student student) {
        return StudentDto.Result.builder()
            .id(student.getId())
            .account(accountDtoMapper.asResultDto(student.getAccount()))
            .parentPhone(student.getParentPhone())
            .schoolName(student.getSchoolName())
            .schoolYear(student.getSchoolYear())
            .build();
    }

}
