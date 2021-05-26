package kr.pullgo.pullgoserver.helper;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.UserRole;

public class AccountHelper {

    private static final String ARBITRARY_USERNAME = "woodyn1002";
    private static final String ARBITRARY_PASSWORD = "this!sPassw0rd";
    private static final String ARBITRARY_FULL_NAME = "최우진";
    private static final String ARBITRARY_PHONE = "01012345678";

    public static Account anAccount() {
        Account account = Account.builder()
            .username(ARBITRARY_USERNAME)
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
            .username(ARBITRARY_USERNAME)
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
        return AccountDto.Result.builder()
            .username(ARBITRARY_USERNAME)
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .role(UserRole.USER)
            .build();
    }

}
