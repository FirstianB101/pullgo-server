package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public interface LessonDto {

    @Data
    @Builder
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
    class Update {

        private String name;

        private ScheduleDto.Update schedule;
    }

    @Data
    @Builder
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
