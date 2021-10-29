package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.MultipleChoice;
import kr.pullgo.pullgoserver.persistence.model.Question;
import org.junit.jupiter.api.Test;

class QuestionDtoMapperTest {

    private final QuestionDtoMapper dtoMapper = new QuestionDtoMapper();

    @Test
    void asEntity() {
        // When
        QuestionDto.Create dto = QuestionDto.Create.builder()
            .examId(0L)
            .questionConfig(QuestionDto.QuestionConfig.builder()
                .content("test content")
                .pictureUrl(null)
                .answer(Set.of(1, 3))
                .choice(Map.of(
                    "1", "1", "2", "2", "3", "3", "4", "4", "5", "5"))
                .build())
            .build();

        Question entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getContent()).isEqualTo("test content");
        assertThat(entity.getPictureUrl()).isNull();
        assertThat(entity.getAnswer().getObjectiveNumbers()).containsOnly(1, 3);
        assertThat(entity.getMultipleChoice().getChoices()).isEqualTo(Map.of(
            "1", "1", "2", "2", "3", "3", "4", "4", "5", "5"));
    }

    @Test
    void asResultDto() {
        // When
        Question entity = Question.builder()
            .content("test content")
            .pictureUrl(null)
            .answer(new Answer(1, 3))
            .multipleChoice(new MultipleChoice("test choice 1", "test choice 2", "test choice 3",
                "test choice 4", "test choice 5"))
            .build();
        entity.setId(0L);
        entity.setExam(anExam().withId(1L));

        QuestionDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getExamId()).isEqualTo(1L);
        assertThat(dto.getQuestionConfig().getContent()).isEqualTo("test content");
        assertThat(dto.getQuestionConfig().getPictureUrl()).isNull();
        assertThat(dto.getQuestionConfig().getAnswer()).containsOnly(1, 3);
        assertThat(dto.getQuestionConfig().getChoice()).isEqualTo(Map.of(
            "1", "test choice 1", "2", "test choice 2", "3", "test choice 3", "4", "test choice 4",
            "5", "test choice 5"));
    }

}