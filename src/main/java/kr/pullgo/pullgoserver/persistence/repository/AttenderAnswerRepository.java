package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AttenderAnswerRepository extends JpaRepository<AttenderAnswer, Long> {

    @Transactional
    int removeById(Long id);
}
