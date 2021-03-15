package kr.pullgo.pullgoserver.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface QuestionDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private Long examId;

        @NonNull
        private String content;

        private String pictureUrl;

        @NonNull
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

        @NonNull
        private Long id;

        @NonNull
        private Long examId;

        @NonNull
        private String content;

        private String pictureUrl;

        @NonNull
        private Set<Integer> answer;
    }
}
