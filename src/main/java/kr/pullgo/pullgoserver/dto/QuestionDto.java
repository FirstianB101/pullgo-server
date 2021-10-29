package kr.pullgo.pullgoserver.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface QuestionDto {

    @Data
    @Builder
    @With
    class QuestionConfig {

        @NotNull
        private String content;

        private String pictureUrl;

        @NotNull
        private Set<Integer> answer;
        @NotNull
        private Map<String, String> choice;

    }

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private Long examId;

        @NotNull
        private QuestionConfig questionConfig;
    }

    @Data
    @Builder
    @With
    class MultipleCreate {

        @NotNull
        private Long examId;

        @NotNull
        private List<QuestionDto.QuestionConfig> questionConfigs;
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

        @NotNull
        private QuestionConfig questionConfig;
    }
}
