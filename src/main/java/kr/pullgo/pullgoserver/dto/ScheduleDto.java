package kr.pullgo.pullgoserver.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public interface ScheduleDto {

    @Data
    @Builder
    class Create {

        @NotNull
        private LocalDate date;

        @NotNull
        private LocalTime beginTime;

        @NotNull
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

        @NotNull
        private LocalDate date;

        @NotNull
        private LocalTime beginTime;

        @NotNull
        private LocalTime endTime;
    }
}
