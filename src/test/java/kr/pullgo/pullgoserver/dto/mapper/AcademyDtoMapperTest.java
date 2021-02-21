package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import org.junit.jupiter.api.Test;

class AcademyDtoMapperTest {

    private final AcademyDtoMapper dtoMapper = new AcademyDtoMapper();

    @Test
    void asEntity() {
        // When
        AcademyDto.Create dto = AcademyDto.Create.builder()
            .name("test name")
            .phone("01012345678")
            .address("test address")
            .build();

        Academy entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getName()).isEqualTo("test name");
        assertThat(entity.getPhone()).isEqualTo("01012345678");
        assertThat(entity.getAddress()).isEqualTo("test address");
    }

    @Test
    void asResultDto() {
        // When
        Academy entity = Academy.builder()
            .name("test name")
            .phone("01012345678")
            .address("test address")
            .build();
        entity.setId(0L);

        AcademyDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isEqualTo("test name");
        assertThat(dto.getPhone()).isEqualTo("01012345678");
        assertThat(dto.getAddress()).isEqualTo("test address");
    }

}