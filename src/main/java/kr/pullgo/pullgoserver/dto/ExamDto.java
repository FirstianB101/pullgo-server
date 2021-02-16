package kr.pullgo.pullgoserver.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface ExamDto {

    static ExamDto.Result mapFromEntity(Exam exam) {
        return Result.builder()
            .id(exam.getId())
            .classroomId(exam.getId())
            .creator(TeacherDto.mapFromEntity(exam.getCreator()))
            .name(exam.getName())
            .beginDateTime(exam.getBeginDateTime())
            .endDateTime(exam.getEndDateTime())
            .timeLimit(exam.getTimeLimit())
            .passScore(exam.getPassScore())
            .cancelled(exam.isCancelled())
            .finished(exam.isFinished())
            .build();
    }

    static Exam mapToEntity(ExamDto.Create dto) {
        return Exam.builder()
            .name(dto.getName())
            .beginDateTime(dto.getBeginDateTime())
            .endDateTime(dto.getEndDateTime())
            .timeLimit(dto.getTimeLimit())
            .build();
    }

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
