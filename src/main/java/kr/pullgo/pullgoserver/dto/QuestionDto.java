package kr.pullgo.pullgoserver.dto;

import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public interface QuestionDto {

    @Data
    @Builder
    class Create {

        @NotNull
        private Long examId;

        @NotNull
        private String content;

        private String pictureUrl;

        @NotNull
        private Set<Integer> answer;
    }

    @Data
    @Builder
    class Update {

        private String content;

        private String pictureUrl;

        private Set<Integer> answer;
    }

    @Data
    @Builder
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private Long examId;

        @NotNull
        private String content;

        private String pictureUrl;

        @NotNull
        private Set<Integer> answer;
    }
}
