package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.MultipleChoice;
import kr.pullgo.pullgoserver.persistence.model.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionDtoMapper implements
    DtoMapper<Question, QuestionDto.Create, QuestionDto.Result> {

    @Override
    public Question asEntity(QuestionDto.Create dto) {
        return Question.builder()
            .content(dto.getQuestionConfig().getContent())
            .pictureUrl(dto.getQuestionConfig().getPictureUrl())
            .answer(new Answer(dto.getQuestionConfig().getAnswer()))
            .multipleChoice(new MultipleChoice(dto.getQuestionConfig().getChoice()))
            .build();
    }

    @Override
    public QuestionDto.Result asResultDto(Question question) {
        return QuestionDto.Result.builder()
            .id(question.getId())
            .examId(question.getExam().getId())
            .questionConfig(QuestionDto.QuestionConfig.builder()
                .content(question.getContent())
                .pictureUrl(question.getPictureUrl())
                .answer(question.getAnswer().getObjectiveNumbers())
                .choice(question.getMultipleChoice().getChoices())
                .build())
            .build();
    }

}
