package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;

import java.util.Set;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Question;

public class QuestionHelper {

    public static Question aQuestion() {
        Question question = Question.builder()
            .content("Contents")
            .pictureUrl("Url")
            .answer(new Answer(1, 2, 3))
            .build();
        question.setId(0L);
        question.setExam(anExam());
        return question;
    }

    public static QuestionDto.Create aQuestionCreateDto() {
        return QuestionDto.Create.builder()
            .examId(0L)
            .content("Contents")
            .pictureUrl("Url")
            .answer(Set.of(1, 2, 3))
            .build();
    }

    public static QuestionDto.Update aQuestionUpdateDto() {
        return QuestionDto.Update.builder()
            .content("Contents")
            .pictureUrl("Url")
            .answer(Set.of(1, 2, 3))
            .build();
    }

    public static QuestionDto.Result aQuestionResultDto() {
        return QuestionDto.Result.builder()
            .id(0L)
            .examId(0L)
            .content("Contents")
            .pictureUrl("Url")
            .answer(Set.of(1, 2, 3))
            .build();
    }

}
