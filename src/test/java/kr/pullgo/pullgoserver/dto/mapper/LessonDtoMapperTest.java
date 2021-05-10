package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aSchedule;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aScheduleCreateDto;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aScheduleResultDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
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
            .willReturn(aSchedule().withId(0L));

        // When
        LessonDto.Create dto = LessonDto.Create.builder()
            .classroomId(0L)
            .name("test name")
            .schedule(aScheduleCreateDto())
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
            .willReturn(aScheduleResultDto());

        // When
        Lesson entity = Lesson.builder()
            .name("test name")
            .build();
        entity.setId(0L);
        entity.setSchedule(aSchedule().withId(1L));
        entity.setClassroom(aClassroom().withId(2L));

        LessonDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isEqualTo("test name");
        assertThat(dto.getClassroomId()).isEqualTo(2L);
    }

}