package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Transactional
    int removeById(Long id);
}
