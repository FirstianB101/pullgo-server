package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyWithId;
import static org.assertj.core.api.Assertions.assertThat;

import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import org.junit.jupiter.api.Test;

class ClassroomDtoMapperTest {

    private final ClassroomDtoMapper dtoMapper = new ClassroomDtoMapper();

    @Test
    void asEntity() {
        // When
        ClassroomDto.Create dto = ClassroomDto.Create.builder()
            .name("test name")
            .academyId(0L)
            .build();

        Classroom entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getName()).isEqualTo("test name");
    }

    @Test
    void asResultDto() {
        // When
        Classroom entity = Classroom.builder()
            .name("test name")
            .build();
        entity.setId(0L);
        entity.setAcademy(academyWithId(1L));

        ClassroomDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isEqualTo("test name");
        assertThat(dto.getAcademyId()).isEqualTo(1L);
    }

}