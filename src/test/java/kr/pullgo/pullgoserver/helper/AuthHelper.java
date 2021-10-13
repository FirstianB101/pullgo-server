package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;

import java.util.function.Function;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class AuthHelper {

    private final JwtService jwtService;

    public String generateToken(Function<? super Account, ? extends Account> initialize) {
        Account account = anAccount().withId(null);

        account = initialize.apply(account);
        return jwtService.signJwt(account);
    }
}
