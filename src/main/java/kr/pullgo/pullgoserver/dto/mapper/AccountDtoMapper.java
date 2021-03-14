package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountDtoMapper implements DtoMapper<Account, AccountDto.Create, AccountDto.Result> {

    @Override
    public Account asEntity(AccountDto.Create dto) {
        return Account.builder()
            .username(dto.getUsername())
            .password(dto.getPassword())
            .fullName(dto.getFullName())
            .phone(dto.getPhone())
            .build();
    }

    @Override
    public AccountDto.Result asResultDto(Account account) {
        return AccountDto.Result.builder()
            .id(account.getId())
            .username(account.getUsername())
            .fullName(account.getFullName())
            .phone(account.getPhone())
            .build();
    }

}
