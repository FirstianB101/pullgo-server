package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonDtoMapperTest {

    @Mock
    private ScheduleDtoMapper scheduleDtoMapper;

    @InjectMocks
    private LessonDtoMapper dtoMapper;

    @Test
    void asEntity() {
        // Given
        given(scheduleDtoMapper.asEntity(any(ScheduleDto.Create.class)))
            .willReturn(scheduleWithId(0L));

        // When
        LessonDto.Create dto = LessonDto.Create.builder()
            .classroomId(0L)
            .name("test name")
            .schedule(scheduleCreateDto())
            .build();

        Lesson entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getName()).isEqualTo("test name");
        assertThat(entity.getSchedule().getId()).isEqualTo(0L);
    }

    @Test
    void asResultDto() {
        // Given
        given(scheduleDtoMapper.asResultDto(any(Schedule.class)))
            .willReturn(scheduleResultDtoWithId(1L));

        // When
        Lesson entity = Lesson.builder()
            .name("test name")
            .build();
        entity.setId(0L);
        entity.setSchedule(scheduleWithId(1L));
        entity.setClassroom(classroomWithId(2L));

        LessonDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isEqualTo("test name");
        assertThat(dto.getClassroomId()).isEqualTo(2L);
    }

    private Schedule scheduleWithId(Long id) {
        Schedule schedule = Schedule.builder()
            .date(LocalDate.of(2021, 2, 15))
            .beginTime(LocalTime.of(16, 0))
            .endTime(LocalTime.of(17, 0))
            .build();
        schedule.setId(id);
        return schedule;
    }

    private ScheduleDto.Create scheduleCreateDto() {
        return ScheduleDto.Create.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
    }

    private ScheduleDto.Result scheduleResultDtoWithId(Long id) {
        return ScheduleDto.Result.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
    }

    private Classroom classroomWithId(Long id) {
        Classroom classroom = Classroom.builder()
            .name("Test")
            .build();
        classroom.setId(id);
        return classroom;
    }

}