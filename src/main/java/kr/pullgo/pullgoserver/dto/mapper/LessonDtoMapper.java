package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LessonDtoMapper implements DtoMapper<Lesson, LessonDto.Create, LessonDto.Result> {

    private final ScheduleDtoMapper scheduleDtoMapper;

    @Autowired
    public LessonDtoMapper(ScheduleDtoMapper scheduleDtoMapper) {
        this.scheduleDtoMapper = scheduleDtoMapper;
    }

    @Override
    public Lesson asEntity(LessonDto.Create dto) {
        Lesson lesson = Lesson.builder()
            .name(dto.getName())
            .build();
        lesson.setSchedule(scheduleDtoMapper.asEntity(dto.getSchedule()));
        return lesson;
    }

    @Override
    public LessonDto.Result asResultDto(Lesson lesson) {
        return LessonDto.Result.builder()
            .id(lesson.getId())
            .schedule(scheduleDtoMapper.asResultDto(lesson.getSchedule()))
            .name(lesson.getName())
            .classroomId(lesson.getClassroom().getId())
            .build();
    }

}
