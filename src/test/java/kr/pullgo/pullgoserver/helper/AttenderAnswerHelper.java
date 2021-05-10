package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AttenderStateHelper.anAttenderState;
import static kr.pullgo.pullgoserver.helper.QuestionHelper.aQuestion;

import java.util.Set;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;

public class AttenderAnswerHelper {

    public static AttenderAnswer anAttenderAnswer() {
        AttenderAnswer attenderAnswer = AttenderAnswer.builder()
            .answer(new Answer(4, 5, 6))
            .build();
        attenderAnswer.setId(0L);
        attenderAnswer.setAttenderState(anAttenderState());
        attenderAnswer.setQuestion(aQuestion());
        return attenderAnswer;
    }

    public static AttenderAnswerDto.Create anAttenderAnswerCreateDto() {
        return AttenderAnswerDto.Create.builder()
            .attenderStateId(0L)
            .questionId(0L)
            .answer(Set.of(4, 5, 6))
            .build();
    }

    public static AttenderAnswerDto.Update anAttenderAnswerUpdateDto() {
        return AttenderAnswerDto.Update.builder()
            .answer(Set.of(4, 5, 6))
            .build();
    }

    public static AttenderAnswerDto.Result anAttenderAnswerResultDto() {
        return AttenderAnswerDto.Result.builder()
            .id(0L)
            .attenderStateId(0L)
            .questionId(0L)
            .answer(Set.of(4, 5, 6))
            .build();
    }

}
