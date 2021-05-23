package kr.pullgo.pullgoserver.service;

import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import kr.pullgo.pullgoserver.config.security.UserPrincipal;
import kr.pullgo.pullgoserver.persistence.model.Account;
import org.junit.jupiter.api.Test;

public class JwtServiceTest {

    private static final String SECRET = "Z1VrWHAyczV2OHkvQj9FKEgrTWJRZVNoVm1ZcTN0Nnc=";
    private static final int EXP_INTERVAL = 60 * 60 * 24 * 7; // a week

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new ParameterNamesModule());

    private final JwtService jwtService = new JwtService(objectMapper, SECRET, EXP_INTERVAL);

    @Test
    void extractSubject() {
        // Given
        Account account = anAccount();
        String jwt = jwtService.signJwt(account);

        // When
        UserPrincipal principal = jwtService.extractSubject(jwt);

        // Then
        assertThat(principal.getAccountId())
            .isEqualTo(account.getId());

        assertThat(principal.getUsername())
            .isEqualTo(account.getUsername());
    }
}
