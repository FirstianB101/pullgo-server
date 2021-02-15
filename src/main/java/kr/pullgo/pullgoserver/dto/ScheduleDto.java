package kr.pullgo.pullgoserver.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface ScheduleDto {

    static ScheduleDto.Result mapFromEntity(Schedule schedule) {
        return Result.builder()
            .id(schedule.getId())
            .date(schedule.getDate())
            .beginTime(schedule.getBeginTime())
            .endTime(schedule.getEndTime())
            .build();
    }

    static Schedule mapToEntity(ScheduleDto.Create dto) {
        return Schedule.builder()
            .date(dto.getDate())
            .beginTime(dto.getBeginTime())
            .endTime(dto.getEndTime())
            .build();
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private LocalDate date;

        @NonNull
        private LocalTime beginTime;

        @NonNull
        private LocalTime endTime;
    }

    @Data
    @Builder
    class Update {

        private LocalDate date;

        private LocalTime beginTime;

        private LocalTime endTime;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private LocalDate date;

        @NonNull
        private LocalTime beginTime;

        @NonNull
        private LocalTime endTime;
    }
}
