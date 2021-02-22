package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.Lesson;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends BaseRepository<Lesson, Long> {

}
