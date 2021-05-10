package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;

import java.util.Set;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Question;

public class QuestionHelper {

    private static final String ARBITRARY_CONTENT =
        "2보다 작은 자연수는?\n(단, 0은 자연수가 아니다)";
    private static final String ARBITRARY_PICTURE_URL =
        "https://avatars.githubusercontent.com/u/77564110?s=200&v=4";
    private static final Set<Integer> ARBITRARY_ANSWER = Set.of(1);

    public static Question aQuestion() {
        Question question = Question.builder()
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(new Answer(ARBITRARY_ANSWER))
            .build();
        question.setId(0L);
        question.setExam(anExam());
        return question;
    }

    public static QuestionDto.Create aQuestionCreateDto() {
        return QuestionDto.Create.builder()
            .examId(0L)
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(ARBITRARY_ANSWER)
            .build();
    }

    public static QuestionDto.Update aQuestionUpdateDto() {
        return QuestionDto.Update.builder()
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(ARBITRARY_ANSWER)
            .build();
    }

    public static QuestionDto.Result aQuestionResultDto() {
        return QuestionDto.Result.builder()
            .id(0L)
            .examId(0L)
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(ARBITRARY_ANSWER)
            .build();
    }

}
