package kr.pullgo.pullgoserver.dto;

import java.util.Set;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AttenderAnswerDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private Long attenderStateId;

        @NonNull
        private Long questionId;

        @NonNull
        private Set<Integer> answer;
    }

    @Data
    @Builder
    class Update {

        private Set<Integer> answer;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private Long attenderStateId;

        @NonNull
        private Long questionId;

        @NonNull
        private Answer answer;
    }
}
