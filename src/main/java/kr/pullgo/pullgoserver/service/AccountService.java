package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.mapper.AccountDtoMapper;
import kr.pullgo.pullgoserver.error.exception.AccountAlreadyEnrolledException;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.UserRole;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountDtoMapper dtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AccountService(AccountDtoMapper dtoMapper, PasswordEncoder passwordEncoder,
        AccountRepository accountRepository,
        ServiceErrorHelper errorHelper) {
        this.dtoMapper = dtoMapper;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.errorHelper = errorHelper;
    }

    public Account create(AccountDto.Create dto) {
        try {
            checkAlreadyEnrollment(dto.getUsername());
        } catch (AccountAlreadyEnrolledException e) {
            throw errorHelper.badRequest("Already enrolled account");
        }
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

    public Boolean checkDuplicateUsername(String username) {
        try {
            checkAlreadyEnrollment(username);
        } catch (AccountAlreadyEnrolledException e) {
            return true;
        }
        return false;
    }

    private void checkAlreadyEnrollment(String username) {
        Optional<Account> sameName = accountRepository.findByUsername(username);
        sameName.ifPresent(u -> {
            throw new AccountAlreadyEnrolledException();
        });
    }
}
