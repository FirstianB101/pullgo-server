package kr.pullgo.pullgoserver.dto;

import java.util.Set;
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
            .questionId(attenderAnswer.getQuestion().getId())
            .answer(attenderAnswer.getAnswer())
            .build();
    }

    static AttenderAnswer mapToEntity(AttenderAnswerDto.Create dto) {
        return AttenderAnswer.builder()
            .answer(new Answer(dto.getAnswer()))
            .build();
    }

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
