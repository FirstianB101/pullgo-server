package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import org.springframework.stereotype.Component;

@Component
public class AcademyDtoMapper implements DtoMapper<Academy, AcademyDto.Create, AcademyDto.Result> {

    @Override
    public Academy asEntity(AcademyDto.Create dto) {
        return Academy.builder()
            .name(dto.getName())
            .phone(dto.getPhone())
            .address(dto.getAddress())
            .build();
    }

    @Override
    public AcademyDto.Result asResultDto(Academy academy) {
        return AcademyDto.Result.builder()
            .id(academy.getId())
            .name(academy.getName())
            .phone(academy.getPhone())
            .address(academy.getAddress())
            .ownerId(academy.getOwner().getId())
            .build();
    }

}
