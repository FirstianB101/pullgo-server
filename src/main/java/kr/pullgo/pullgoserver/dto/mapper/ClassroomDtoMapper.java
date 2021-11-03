package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClassroomDtoMapper implements
    DtoMapper<Classroom, ClassroomDto.Create, ClassroomDto.Result> {

    private final TeacherDtoMapper teacherDtoMapper;

    @Autowired
    public ClassroomDtoMapper(TeacherDtoMapper teacherDtoMapper) {
        this.teacherDtoMapper = teacherDtoMapper;
    }

    @Override
    public Classroom asEntity(ClassroomDto.Create dto) {
        return Classroom.builder()
            .name(dto.getName())
            .build();
    }

    @Override
    public ClassroomDto.Result asResultDto(Classroom classroom) {
        return ClassroomDto.Result.builder()
            .id(classroom.getId())
            .name(classroom.getName())
            .creator(teacherDtoMapper.asResultDto(classroom.getCreator()))
            .academyId(classroom.getAcademy().getId())
            .build();
    }

}
