package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.MultipleChoice;
import kr.pullgo.pullgoserver.persistence.model.Question;

public class QuestionHelper {

    private static final String ARBITRARY_CONTENT =
        "2보다 작은 자연수는?\n(단, 0은 자연수가 아니다)";
    private static final String ARBITRARY_PICTURE_URL =
        "https://avatars.githubusercontent.com/u/77564110?s=200&v=4";
    private static final Set<Integer> ARBITRARY_ANSWER = Set.of(1);

    private static final Map<String ,String> ARBITRARY_MULTIPLE_CHOICE = Map.of(
        "1", "1", "2", "2", "3", "3", "4", "4", "5", "5");

    public static Question aQuestion() {
        Question question = Question.builder()
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(new Answer(ARBITRARY_ANSWER))
            .multipleChoice(new MultipleChoice(ARBITRARY_MULTIPLE_CHOICE))
            .build();
        question.setId(0L);
        question.setExam(anExam());
        return question;
    }

    public static QuestionDto.Create aQuestionCreateDto() {
        return QuestionDto.Create.builder()
            .examId(0L)
            .questionConfig(aQuestionConfigDto())
            .build();
    }

    public static QuestionDto.QuestionConfig aQuestionConfigDto() {
        return QuestionDto.QuestionConfig.builder()
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(ARBITRARY_ANSWER)
            .choice(ARBITRARY_MULTIPLE_CHOICE)
            .build();
    }

    public static QuestionDto.MultipleCreate multipleQuestionCreateDto() {
        return QuestionDto.MultipleCreate.builder()
            .examId(0L)
            .questionConfigs(List.of(
                aQuestionConfigDto(),
                aQuestionConfigDto(),
                aQuestionConfigDto()))
            .build();
    }

    public static QuestionDto.Update aQuestionUpdateDto() {
        return QuestionDto.Update.builder()
            .content(ARBITRARY_CONTENT)
            .pictureUrl(ARBITRARY_PICTURE_URL)
            .answer(ARBITRARY_ANSWER)
            .choice(ARBITRARY_MULTIPLE_CHOICE)
            .build();
    }

    public static QuestionDto.Result aQuestionResultDto() {
        return QuestionDto.Result.builder()
            .id(0L)
            .examId(0L)
            .questionConfig(aQuestionConfigDto())
            .build();
    }

}
