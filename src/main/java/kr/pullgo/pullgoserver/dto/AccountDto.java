package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotEmpty;
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

        @NotEmpty
        private String username;

        @NotEmpty
        private String password;

        @NotEmpty
        private String fullName;

        @NotEmpty
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

        @NotEmpty
        private String username;

        @NotEmpty
        private String fullName;

        @NotEmpty
        private String phone;

        @NotEmpty
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
