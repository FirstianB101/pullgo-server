package kr.pullgo.pullgoserver.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface ScheduleDto {

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
