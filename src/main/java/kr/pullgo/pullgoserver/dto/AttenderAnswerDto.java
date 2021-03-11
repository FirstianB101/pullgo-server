package kr.pullgo.pullgoserver.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @NoArgsConstructor
    class Update {

        private Set<Integer> answer;

        public Update(Set<Integer> answer) {
            this.answer = answer;
        }
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
        private Set<Integer> answer;
    }
}
