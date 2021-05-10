package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamDtoMapperTest {

    @Mock
    private TeacherDtoMapper teacherDtoMapper;

    @InjectMocks
    private ExamDtoMapper dtoMapper;

    @Test
    void asEntity() {
        // When
        ExamDto.Create dto = ExamDto.Create.builder()
            .classroomId(0L)
            .creatorId(1L)
            .name("test name")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ofHours(1))
            .passScore(null)
            .build();

        Exam entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getName()).isEqualTo("test name");
        assertThat(entity.getBeginDateTime())
            .isEqualTo(LocalDateTime.of(2021, 1, 28, 0, 0));
        assertThat(entity.getEndDateTime())
            .isEqualTo(LocalDateTime.of(2021, 1, 29, 0, 0));
        assertThat(entity.getTimeLimit()).isEqualTo(Duration.ofHours(1));
        assertThat(entity.getPassScore()).isNull();
    }

    @Test
    void asResultDto() {

        // When
        Exam entity = Exam.builder()
            .name("test name")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ofHours(1))
            .passScore(null)
            .build();
        entity.setId(0L);
        entity.setClassroom(aClassroom().withId(1L));
        entity.setCreator(aTeacher().withId(2L));
        entity.setCancelled(false);
        entity.setFinished(true);

        ExamDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getClassroomId()).isEqualTo(1L);
        assertThat(dto.getCreatorId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("test name");
        assertThat(dto.getBeginDateTime())
            .isEqualTo(LocalDateTime.of(2021, 1, 28, 0, 0));
        assertThat(dto.getEndDateTime())
            .isEqualTo(LocalDateTime.of(2021, 1, 29, 0, 0));
        assertThat(dto.getTimeLimit()).isEqualTo(Duration.ofHours(1));
    }

}