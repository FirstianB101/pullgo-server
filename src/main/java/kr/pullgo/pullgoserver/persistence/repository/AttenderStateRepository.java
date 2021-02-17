package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AttenderStateRepository extends JpaRepository<AttenderState, Long> {

    @Transactional
    int removeById(Long id);
}
