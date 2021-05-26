package kr.pullgo.pullgoserver.persistence.repository;

import java.util.Optional;
import kr.pullgo.pullgoserver.persistence.model.Account;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends BaseRepository<Account, Long> {

    Optional<Account> findByUsername(String username);

}
