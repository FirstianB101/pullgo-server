package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import org.springframework.stereotype.Component;

@Component
public class AttenderAnswerDtoMapper implements
    DtoMapper<AttenderAnswer, AttenderAnswerDto.Create, AttenderAnswerDto.Result> {

    @Override
    public AttenderAnswer asEntity(AttenderAnswerDto.Create dto) {
        return AttenderAnswer.builder()
            .answer(new Answer(dto.getAnswer()))
            .build();
    }

    @Override
    public AttenderAnswerDto.Result asResultDto(AttenderAnswer attenderAnswer) {
        return AttenderAnswerDto.Result.builder()
            .id(attenderAnswer.getId())
            .attenderStateId(attenderAnswer.getAttenderState().getId())
            .questionId(attenderAnswer.getQuestion().getId())
            .answer(attenderAnswer.getAnswer().getObjectiveNumbers())
            .build();
    }

}
