package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.mapper.AccountDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountDtoMapper dtoMapper;

    @Autowired
    public AccountService(AccountDtoMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    public Account create(AccountDto.Create dto) {
        Account entity = dtoMapper.asEntity(dto);

        entity.setRole(UserRole.USER);

        return entity;
    }

    public void update(Account entity, AccountDto.Update dto) {
        if (dto.getPassword() != null) {
            entity.setPassword(dto.getPassword());
        }
        if (dto.getFullName() != null) {
            entity.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
    }

}
