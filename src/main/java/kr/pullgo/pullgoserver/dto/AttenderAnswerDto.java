package kr.pullgo.pullgoserver.dto;

import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface AttenderAnswerDto {

    @Data
    @Builder
    class Create {

        @NotNull
        private Long attenderStateId;

        @NotNull
        private Long questionId;

        @NotNull
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

        @NotNull
        private Long id;

        @NotNull
        private Long attenderStateId;

        @NotNull
        private Long questionId;

        @NotNull
        private Set<Integer> answer;
    }
}
