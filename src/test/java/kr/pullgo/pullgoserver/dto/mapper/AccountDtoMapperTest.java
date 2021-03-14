package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import org.junit.jupiter.api.Test;

public class AccountDtoMapperTest {

    private final AccountDtoMapper dtoMapper = new AccountDtoMapper();

    @Test
    void asEntity() {
        // When
        AccountDto.Create dto = AccountDto.Create.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();

        Account entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity.getUsername()).isEqualTo("testusername");
        assertThat(entity.getPassword()).isEqualTo("testpassword");
        assertThat(entity.getFullName()).isEqualTo("Test FullName");
        assertThat(entity.getPhone()).isEqualTo("01012345678");
    }

    @Test
    void asResultDto() {
        // When
        Account entity = Account.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
        entity.setId(0L);

        AccountDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getUsername()).isEqualTo("testusername");
        assertThat(dto.getFullName()).isEqualTo("Test FullName");
        assertThat(dto.getPhone()).isEqualTo("01012345678");
    }

}
