package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import org.junit.jupiter.api.Test;

class QuestionDtoMapperTest {

    private final QuestionDtoMapper dtoMapper = new QuestionDtoMapper();

    @Test
    void asEntity() {
        // When
        QuestionDto.Create dto = QuestionDto.Create.builder()
            .examId(0L)
            .content("test content")
            .pictureUrl(null)
            .answer(new Answer(1, 3))
            .build();

        Question entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getContent()).isEqualTo("test content");
        assertThat(entity.getPictureUrl()).isNull();
        assertThat(entity.getAnswer().getObjectiveNumbers()).containsOnly(1, 3);
    }

    @Test
    void asResultDto() {
        // When
        Question entity = Question.builder()
            .content("test content")
            .pictureUrl(null)
            .answer(new Answer(1, 3))
            .build();
        entity.setId(0L);
        entity.setExam(examWithId(1L));

        QuestionDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getExamId()).isEqualTo(1L);
        assertThat(dto.getContent()).isEqualTo("test content");
        assertThat(dto.getPictureUrl()).isNull();
        assertThat(dto.getAnswer().getObjectiveNumbers()).containsOnly(1, 3);
    }

    private Exam examWithId(Long id) {
        Exam exam = Exam.builder()
            .name("Test")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ZERO)
            .build();
        exam.setId(id);
        return exam;
    }

}