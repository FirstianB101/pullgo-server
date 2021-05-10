package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.AttenderStateHelper.anAttenderState;
import static kr.pullgo.pullgoserver.helper.QuestionHelper.aQuestion;
import static org.assertj.core.api.Assertions.assertThat;

import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

public class AttenderAnswerDtoMapperTest {

    private final AttenderAnswerDtoMapper dtoMapper = new AttenderAnswerDtoMapper();

    @Test
    void asEntity() {
        // When
        AttenderAnswerDto.Create dto = AttenderAnswerDto.Create.builder()
            .attenderStateId(0L)
            .questionId(0L)
            .answer(Sets.newSet(1, 3))
            .build();

        AttenderAnswer entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getAnswer().getObjectiveNumbers()).containsOnly(1, 3);
    }

    @Test
    void asResultDto() {
        // When
        AttenderAnswer entity = AttenderAnswer.builder()
            .answer(new Answer(1, 3))
            .build();
        entity.setId(0L);
        entity.setAttenderState(anAttenderState().withId(1L));
        entity.setQuestion(aQuestion().withId(2L));

        AttenderAnswerDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getAttenderStateId()).isEqualTo(1L);
        assertThat(dto.getQuestionId()).isEqualTo(2L);
        assertThat(dto.getAnswer()).containsOnly(1, 3);
    }

}
