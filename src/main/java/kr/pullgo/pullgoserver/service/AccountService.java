package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.mapper.AccountDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountDtoMapper dtoMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountDtoMapper dtoMapper, PasswordEncoder passwordEncoder) {
        this.dtoMapper = dtoMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Account create(AccountDto.Create dto) {
        Account entity = dtoMapper.asEntity(dto);

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        entity.setPassword(encodedPassword);

        entity.setRole(UserRole.USER);

        return entity;
    }

    public void update(Account entity, AccountDto.Update dto) {
        if (dto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            entity.setPassword(encodedPassword);
        }
        if (dto.getFullName() != null) {
            entity.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
    }

}
