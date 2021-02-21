package kr.pullgo.pullgoserver.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface ExamDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private Long classroomId;

        @NonNull
        private TeacherDto.Create creator;

        @NonNull
        private String name;

        @NonNull
        private LocalDateTime beginDateTime;

        @NonNull
        private LocalDateTime endDateTime;

        @NonNull
        private Duration timeLimit;

        private Integer passScore;
    }

    @Data
    @Builder
    class Update {

        private String name;

        private LocalDateTime beginDateTime;

        private LocalDateTime endDateTime;

        private Duration timeLimit;

        private Integer passScore;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private Long classroomId;

        @NonNull
        private TeacherDto.Result creator;

        @NonNull
        private String name;

        @NonNull
        private LocalDateTime beginDateTime;

        @NonNull
        private LocalDateTime endDateTime;

        @NonNull
        private Duration timeLimit;

        private Integer passScore;

        @NonNull
        private Boolean cancelled;

        @NonNull
        private Boolean finished;
    }
}
