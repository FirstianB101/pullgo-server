package kr.pullgo.pullgoserver.dto.mapper;

import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountCreateDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountResultDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentDtoMapperTest {

    @Mock
    private AccountDtoMapper accountDtoMapper;

    @InjectMocks
    private StudentDtoMapper dtoMapper;

    @Test
    void asEntity() {
        // Given
        given(accountDtoMapper.asEntity(any(AccountDto.Create.class)))
            .willReturn(anAccount().withId(0L));

        // When
        StudentDto.Create dto = StudentDto.Create.builder()
            .account(anAccountCreateDto())
            .parentPhone("01012345678")
            .schoolName("test school")
            .schoolYear(1)
            .build();

        Student entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getParentPhone()).isEqualTo("01012345678");
        assertThat(entity.getSchoolName()).isEqualTo("test school");
        assertThat(entity.getSchoolYear()).isEqualTo(1);
        assertThat(entity.getAccount().getId()).isEqualTo(0L);
    }

    @Test
    void asResultDto() {
        // Given
        given(accountDtoMapper.asResultDto(any(Account.class)))
            .willReturn(anAccountResultDto());

        // When
        Student entity = Student.builder()
            .parentPhone("01012345678")
            .schoolName("test school")
            .schoolYear(1)
            .build();
        entity.setId(0L);
        entity.setAccount(anAccount().withId(1L));

        StudentDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getParentPhone()).isEqualTo("01012345678");
        assertThat(dto.getSchoolName()).isEqualTo("test school");
        assertThat(dto.getSchoolYear()).isEqualTo(1);
        assertThat(dto.getAccount()).isNotNull();
    }

}