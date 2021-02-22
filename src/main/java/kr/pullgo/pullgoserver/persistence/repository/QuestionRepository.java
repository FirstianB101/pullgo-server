package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.Question;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends BaseRepository<Question, Long> {

}
