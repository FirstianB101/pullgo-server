package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherDtoMapperTest {

    @Mock
    private AccountDtoMapper accountDtoMapper;

    @InjectMocks
    private TeacherDtoMapper dtoMapper;

    @Test
    void asEntity() {
        // Given
        given(accountDtoMapper.asEntity(any(AccountDto.Create.class)))
            .willReturn(accountWithId(0L));

        // When
        TeacherDto.Create dto = TeacherDto.Create.builder()
            .account(accountCreateDto())
            .build();

        Teacher entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getAccount().getId()).isEqualTo(0L);
    }

    @Test
    void asResultDto() {
        // Given
        given(accountDtoMapper.asResultDto(any(Account.class)))
            .willReturn(accountResultDto());

        // When
        Teacher entity = new Teacher();
        entity.setId(0L);
        entity.setAccount(accountWithId(1L));

        TeacherDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getAccount()).isNotNull();
    }

    private Account accountWithId(Long id) {
        Account account = Account.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
        account.setId(id);
        return account;
    }

    private AccountDto.Create accountCreateDto() {
        return AccountDto.Create.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

    private AccountDto.Result accountResultDto() {
        return AccountDto.Result.builder()
            .username("testusername")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

}