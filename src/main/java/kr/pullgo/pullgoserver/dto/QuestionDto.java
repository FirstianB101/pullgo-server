package kr.pullgo.pullgoserver.dto;

import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface QuestionDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private Long examId;

        @NotEmpty
        private String content;

        private String pictureUrl;

        @NotEmpty
        private Set<Integer> answer;

        @NotEmpty
        private Map<String, String> choice;
    }

    @Data
    @Builder
    @With
    class Update {

        private String content;

        private String pictureUrl;

        private Set<Integer> answer;

        private Map<String, String> choice;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private Long examId;

        @NotEmpty
        private String content;

        private String pictureUrl;

        @NotEmpty
        private Set<Integer> answer;

        @NotEmpty
        private Map<String, String> choice;
    }
}
