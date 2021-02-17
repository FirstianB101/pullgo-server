package kr.pullgo.pullgoserver.dto;

import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AttenderAnswerDto {

    static AttenderAnswerDto.Result mapFromEntity(AttenderAnswer attenderAnswer) {
        return AttenderAnswerDto.Result.builder()
            .id(attenderAnswer.getId())
            .attenderStateId(attenderAnswer.getAttenderState().getId())
            .question(QuestionDto.mapFromEntity(attenderAnswer.getQuestion()))
            .answer(attenderAnswer.getAnswer())
            .build();
    }

    static AttenderAnswer mapToEntity(AttenderAnswerDto.Create dto) {
        AttenderAnswer attenderAnswer = AttenderAnswer.builder()
            .answer(dto.getAnswer())
            .build();
        attenderAnswer.setQuestion(QuestionDto.mapToEntity(dto.getQuestion()));
        return attenderAnswer;
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private Long attenderStateId;

        @NonNull
        private QuestionDto.Create question;

        @NonNull
        private Answer answer;
    }

    @Data
    @Builder
    class Update {

        private QuestionDto.Update question;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private Long attenderStateId;

        @NonNull
        private QuestionDto.Result question;

        @NonNull
        private Answer answer;
    }
}
