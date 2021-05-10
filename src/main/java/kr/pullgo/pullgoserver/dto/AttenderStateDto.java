package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface AttenderStateDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private Long attenderId;

        @NotNull
        private Long examId;
    }

    @Data
    @Builder
    @With
    class Update {

        private AttendingProgress progress;

        private Integer score;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private Long attenderId;

        @NotNull
        private Long examId;

        @NotNull
        private AttendingProgress progress;

        private Integer score;
    }
}
