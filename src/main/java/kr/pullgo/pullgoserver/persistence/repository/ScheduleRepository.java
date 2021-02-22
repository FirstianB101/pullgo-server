package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.Schedule;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends BaseRepository<Schedule, Long> {

}
