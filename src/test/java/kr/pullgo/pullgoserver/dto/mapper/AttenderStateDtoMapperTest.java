package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudent;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import org.junit.jupiter.api.Test;

class AttenderStateDtoMapperTest {

    private final AttenderStateDtoMapper dtoMapper = new AttenderStateDtoMapper();

    @Test
    void asEntity() {
        // When
        AttenderStateDto.Create dto = AttenderStateDto.Create.builder()
            .attenderId(0L)
            .examId(0L)
            .build();

        AttenderState entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity).isNotNull();
    }

    @Test
    void asResultDto() {
        // When
        AttenderState entity = AttenderState.builder().examStartTime(LocalDateTime.now()).build();
        entity.setId(0L);
        entity.setAttender(aStudent().withId(1L));
        entity.setExam(anExam().withId(2L));
        entity.setProgress(AttendingProgress.COMPLETE);
        entity.setScore(100);

        AttenderStateDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getAttenderId()).isEqualTo(1L);
        assertThat(dto.getExamId()).isEqualTo(2L);
        assertThat(dto.getProgress()).isEqualTo(AttendingProgress.COMPLETE);
        assertThat(dto.getScore()).isEqualTo(100);
    }

}