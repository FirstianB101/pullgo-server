package kr.pullgo.pullgoserver.dto;

import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AttenderStateDto {

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
