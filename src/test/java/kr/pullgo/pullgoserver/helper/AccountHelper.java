package kr.pullgo.pullgoserver.helper;

import java.util.UUID;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.UserRole;

public class AccountHelper {

    private static final String ARBITRARY_USERNAME = "user-";
    private static final String ARBITRARY_PASSWORD = "this!sPassw0rd";
    private static final String ARBITRARY_FULL_NAME = "최우진";
    private static final String ARBITRARY_PHONE = "01012345678";

    public static Account anAccount() {
        String uuid = UUID.randomUUID().toString().substring(8);
        Account account = Account.builder()
            .username(ARBITRARY_USERNAME+uuid)
            .password(ARBITRARY_PASSWORD)
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .role(UserRole.USER)
            .build();
        account.setId(0L);
        return account;
    }

    public static AccountDto.Create anAccountCreateDto() {
        String uuid = UUID.randomUUID().toString().substring(8);
        return AccountDto.Create.builder()
            .username(ARBITRARY_USERNAME+uuid)
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
            .username(ARBITRARY_USERNAME+uuid)
            .fullName(ARBITRARY_FULL_NAME)
            .phone(ARBITRARY_PHONE)
            .role(UserRole.USER)
            .build();
    }

}
