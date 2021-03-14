package kr.pullgo.pullgoserver.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AccountDto {

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
        private String username;

        @NonNull
        private String fullName;

        @NonNull
        private String phone;
    }
}
