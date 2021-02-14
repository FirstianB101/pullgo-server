package kr.pullgo.pullgoserver.dto;

import kr.pullgo.pullgoserver.persistence.model.Account;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AccountDto {

    static AccountDto.Result mapFromEntity(Account account) {
        return AccountDto.Result.builder()
            .id(account.getId())
            .username(account.getUsername())
            .password(account.getPassword())
            .fullName(account.getFullName())
            .phone(account.getPhone())
            .build();
    }

    static Account mapToEntity(AccountDto.Create dto) {
        return Account.builder()
            .username(dto.getUsername())
            .password(dto.getPassword())
            .fullName(dto.getFullName())
            .phone(dto.getPhone())
            .build();
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private String username;

        @NonNull
        private String password;

        @NonNull
        private String fullName;

        @NonNull
        private String phone;
    }

    @Data
    @Builder
    class Update {

        private String password;

        private String fullName;

        private String phone;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private String username;

        @NonNull
        private String password;

        @NonNull
        private String fullName;

        @NonNull
        private String phone;
    }
}
