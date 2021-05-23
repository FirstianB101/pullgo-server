package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends BaseRepository<Student, Long> {

    Student findByAccountId(Long accountId);

}
