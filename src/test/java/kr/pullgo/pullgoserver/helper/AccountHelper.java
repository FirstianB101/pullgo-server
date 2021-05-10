package kr.pullgo.pullgoserver.helper;

import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.persistence.model.Account;

public class AccountHelper {

    public static Account anAccount() {
        Account account = Account.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
        account.setId(0L);
        return account;
    }

    public static AccountDto.Create anAccountCreateDto() {
        return AccountDto.Create.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

    public static AccountDto.Update anAccountUpdateDto() {
        return AccountDto.Update.builder()
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

    public static AccountDto.Result anAccountResultDto() {
        return AccountDto.Result.builder()
            .username("testusername")
            .fullName("Test FullName")
            .phone("01012345678")
            .build();
    }

}
