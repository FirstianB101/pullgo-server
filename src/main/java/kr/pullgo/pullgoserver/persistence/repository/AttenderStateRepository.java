package kr.pullgo.pullgoserver.persistence.repository;

import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import org.springframework.stereotype.Repository;

@Repository
public interface AttenderStateRepository extends BaseRepository<AttenderState, Long> {

    boolean existsFindByAttenderIdAndExamId(Long attenderId, Long ExamId);
}
