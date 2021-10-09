package kr.pullgo.pullgoserver.helper;

import java.util.UUID;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.UserRole;

public class AccountHelper {

    private static final String USERNAME_PREFIX = "user-";
    private static final String ARBITRARY_PASSWORD = "this!sPassw0rd";
    private static final String ARBITRARY_FULL_NAME = "최우진";
    private static final String ARBITRARY_PHONE = "01012345678";

    private static String getArbitraryUsername() {
        return USERNAME_PREFIX + getShortUUID();
    }

    private static String getShortUUID() {
        return UUID.randomUUID().toString().substring(8);
    }

    public static Account anAccount() {
        Account account = Account.builder()
            .username(getArbitraryUsername())
            .password(ARBITRARY_PASSWORD)
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .role(UserRole.USER)
            .build();
        account.setId(0L);
        return account;
    }

    public static AccountDto.Create anAccountCreateDto() {
        return AccountDto.Create.builder()
            .username(getArbitraryUsername())
            .password(ARBITRARY_PASSWORD)
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .build();
    }

    public static AccountDto.Update anAccountUpdateDto() {
        return AccountDto.Update.builder()
            .password(ARBITRARY_PASSWORD)
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .build();
    }

    public static AccountDto.Result anAccountResultDto() {
        String uuid = UUID.randomUUID().toString().substring(8);
        return AccountDto.Result.builder()
            .username(getArbitraryUsername())
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .role(UserRole.USER)
            .build();
    }

}
