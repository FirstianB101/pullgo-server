package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TeacherDtoMapper implements DtoMapper<Teacher, TeacherDto.Create, TeacherDto.Result> {

    private final AccountDtoMapper accountDtoMapper;

    @Autowired
    public TeacherDtoMapper(AccountDtoMapper accountDtoMapper) {
        this.accountDtoMapper = accountDtoMapper;
    }

    @Override
    public Teacher asEntity(TeacherDto.Create dto) {
        Teacher teacher = new Teacher();
        teacher.setAccount(accountDtoMapper.asEntity(dto.getAccount()));
        return teacher;
    }

    @Override
    public TeacherDto.Result asResultDto(Teacher teacher) {
        return TeacherDto.Result.builder()
            .id(teacher.getId())
            .account(accountDtoMapper.asResultDto(teacher.getAccount()))
            .build();
    }

}
