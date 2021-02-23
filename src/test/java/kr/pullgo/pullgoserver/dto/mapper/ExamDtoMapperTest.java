package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.TeacherHelper.teacherWithId;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
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
        entity.setClassroom(classroomWithId(1L));
        entity.setCreator(teacherWithId(2L));
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

    private TeacherDto.Create teacherCreateDto() {
        return TeacherDto.Create.builder()
            .account(accountCreateDto())
            .build();
    }

    private TeacherDto.Result teacherResultDtoWithId(Long id) {
        return TeacherDto.Result.builder()
            .id(id)
            .account(accountResultDto())
            .build();
    }

    private AccountDto.Create accountCreateDto() {
        return AccountDto.Create.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

    private AccountDto.Result accountResultDto() {
        return AccountDto.Result.builder()
            .id(0L)
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

    private Classroom classroomWithId(Long id) {
        Classroom classroom = Classroom.builder()
            .name("test name")
            .build();
        classroom.setId(id);
        return classroom;
    }
}