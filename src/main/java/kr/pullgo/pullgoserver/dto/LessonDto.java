package kr.pullgo.pullgoserver.dto;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface LessonDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private Long classroomId;

        @NonNull
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

        @NonNull
        private Long id;

        @NonNull
        private Long classroomId;

        @NonNull
        private String name;

        @NotNull
        private ScheduleDto.Result schedule;
    }
}
