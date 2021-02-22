package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.Classroom;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends BaseRepository<Classroom, Long> {

}
