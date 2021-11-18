package kr.pullgo.pullgoserver.dto;

import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

public interface AttenderAnswerDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private Long attenderStateId;

        @NotNull
        private Long questionId;

        @NotEmpty
        private Set<Integer> answer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @With
    class Update {

        private Set<Integer> answer;

        public Update(Set<Integer> answer) {
            this.answer = answer;
        }
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private Long attenderStateId;

        @NotNull
        private Long questionId;

        @NotEmpty
        private Set<Integer> answer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @With
    class Put {

        @NotEmpty
        private Set<Integer> answer;

        public Put(Set<Integer> answer) {
            this.answer = answer;
        }
    }
}
