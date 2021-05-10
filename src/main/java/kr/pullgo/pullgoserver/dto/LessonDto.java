package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface LessonDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private Long classroomId;

        @NotNull
        private String name;

        @NotNull
        private ScheduleDto.Create schedule;
    }

    @Data
    @Builder
    @With
    class Update {

        private String name;

        private ScheduleDto.Update schedule;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private Long classroomId;

        @NotNull
        private String name;

        @NotNull
        private ScheduleDto.Result schedule;
    }
}
