package kr.pullgo.pullgoserver.dto;

import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Question;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface QuestionDto {

    static QuestionDto.Result mapFromEntity(Question question) {
        return QuestionDto.Result.builder()
            .id(question.getId())
            .examId(question.getExam().getId())
            .content(question.getContent())
            .pictureUrl(question.getPictureUrl())
            .answer(question.getAnswer())
            .build();
    }

    static Question mapToEntity(QuestionDto.Create dto) {
        return Question.builder()
            .content(dto.getContent())
            .pictureUrl(dto.getPictureUrl())
            .answer(dto.getAnswer())
            .build();
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private Long examId;

        @NonNull
        private String content;

        private String pictureUrl;

        @NonNull
        private Answer answer;
    }

    @Data
    @Builder
    class Update {

        private String content;

        private String pictureUrl;

        private Answer answer;
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
        private Answer answer;
    }
}
