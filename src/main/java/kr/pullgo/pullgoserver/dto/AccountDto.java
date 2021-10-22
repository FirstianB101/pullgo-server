package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import kr.pullgo.pullgoserver.persistence.model.UserRole;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface AccountDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private String username;

        @NotNull
        private String password;

        @NotNull
        private String fullName;

        @NotNull
        private String phone;
    }

    @Data
    @Builder
    @With
    class Update {

        private String password;

        private String fullName;

        private String phone;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private String username;

        @NotNull
        private String fullName;

        @NotNull
        private String phone;

        @NotNull
        private UserRole role;
    }

    @Data
    @Builder
    @With
    class CheckDuplicationResult {

        @NotNull
        private Boolean exists;
    }
}
