package kr.pullgo.pullgoserver.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public interface ExamDto {

    @Data
    @Builder
    class Create {

        @NotNull
        private Long classroomId;

        @NotNull
        private Long creatorId;

        @NotNull
        private String name;

        @NotNull
        private LocalDateTime beginDateTime;

        @NotNull
        private LocalDateTime endDateTime;

        @NotNull
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

        @NotNull
        private Long id;

        @NotNull
        private Long classroomId;

        @NotNull
        private Long creatorId;

        @NotNull
        private String name;

        @NotNull
        private LocalDateTime beginDateTime;

        @NotNull
        private LocalDateTime endDateTime;

        @NotNull
        private Duration timeLimit;

        private Integer passScore;

        @NotNull
        private Boolean cancelled;

        @NotNull
        private Boolean finished;
    }
}
