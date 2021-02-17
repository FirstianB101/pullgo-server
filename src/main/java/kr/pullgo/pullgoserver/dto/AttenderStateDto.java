package kr.pullgo.pullgoserver.dto;

import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AttenderStateDto {

    static AttenderStateDto.Result mapFromEntity(AttenderState attenderState) {
        return AttenderStateDto.Result.builder()
            .id(attenderState.getId())
            .attenderId(attenderState.getAttender().getId())
            .examId(attenderState.getExam().getId())
            .progress(attenderState.getProgress())
            .score(attenderState.getScore())
            .submitted(attenderState.isSubmitted())
            .build();
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private Long attenderId;

        @NonNull
        private Long examId;
    }

    @Data
    @Builder
    class Update {

        private AttendingProgress progress;

        private Integer score;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private Long attenderId;

        @NonNull
        private Long examId;

        @NonNull
        private AttendingProgress progress;

        private Integer score;

        @NonNull
        private Boolean submitted;
    }
}
