package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import org.junit.jupiter.api.Test;

class ScheduleDtoMapperTest {

    private final ScheduleDtoMapper dtoMapper = new ScheduleDtoMapper();

    @Test
    void asEntity() {
        // When
        ScheduleDto.Create dto = ScheduleDto.Create.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();

        Schedule entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getDate())
            .isEqualTo(LocalDate.of(2021, 2, 22));
        assertThat(entity.getBeginTime())
            .isEqualTo(LocalTime.of(12, 0));
        assertThat(entity.getEndTime())
            .isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    void asResultDto() {
        // When
        Schedule entity = Schedule.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
        entity.setId(0L);

        ScheduleDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getDate())
            .isEqualTo(LocalDate.of(2021, 2, 22));
        assertThat(dto.getBeginTime())
            .isEqualTo(LocalTime.of(12, 0));
        assertThat(dto.getEndTime())
            .isEqualTo(LocalTime.of(13, 0));
    }

}