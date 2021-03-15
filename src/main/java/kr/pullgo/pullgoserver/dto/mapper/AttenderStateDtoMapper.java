package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import org.springframework.stereotype.Component;

@Component
public class AttenderStateDtoMapper implements
    DtoMapper<AttenderState, AttenderStateDto.Create, AttenderStateDto.Result> {

    @Override
    public AttenderState asEntity(AttenderStateDto.Create dto) {
        return new AttenderState();
    }

    @Override
    public AttenderStateDto.Result asResultDto(AttenderState attenderState) {
        return AttenderStateDto.Result.builder()
            .id(attenderState.getId())
            .attenderId(attenderState.getAttender().getId())
            .examId(attenderState.getExam().getId())
            .progress(attenderState.getProgress())
            .score(attenderState.getScore())
            .build();
    }

}
