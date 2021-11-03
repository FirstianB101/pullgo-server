package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademy;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherResultDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassroomDtoMapperTest {

    @Mock
    private TeacherDtoMapper teacherDtoMapper;

    @InjectMocks
    private ClassroomDtoMapper dtoMapper;

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
        // Given
        given(teacherDtoMapper.asResultDto(any(Teacher.class)))
            .willReturn(aTeacherResultDto().withId(2L));

        // When
        Classroom entity = Classroom.builder()
            .name("test name")
            .build();
        entity.setId(0L);
        entity.setAcademy(anAcademy().withId(1L));
        entity.setCreator(aTeacher().withId(2L));

        ClassroomDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isEqualTo("test name");
        assertThat(dto.getAcademyId()).isEqualTo(1L);
        assertThat(dto.getCreator().getId()).isEqualTo(2L);
    }

}