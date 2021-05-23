package kr.pullgo.pullgoserver.config.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPrincipal {

    private final Long accountId;
    private final String username;

    @Builder
    public UserPrincipal(Long accountId, String username) {
        this.accountId = accountId;
        this.username = username;
    }

}
