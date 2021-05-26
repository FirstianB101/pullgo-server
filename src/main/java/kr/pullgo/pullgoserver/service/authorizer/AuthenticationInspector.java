package kr.pullgo.pullgoserver.service.authorizer;

import kr.pullgo.pullgoserver.config.security.UserPrincipal;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationInspector {

    private final AccountRepository accountRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AuthenticationInspector(
        AccountRepository accountRepository,
        ServiceErrorHelper errorHelper) {
        this.accountRepository = accountRepository;
        this.errorHelper = errorHelper;
    }

    public Account getAccountOrThrow(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw errorHelper.unauthorized("Not authenticated");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return accountRepository.findById(principal.getAccountId())
            .orElseThrow(() -> errorHelper.forbidden("Removed account"));
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
    }

}
