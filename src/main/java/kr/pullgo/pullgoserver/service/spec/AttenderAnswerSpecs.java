package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import org.springframework.data.jpa.domain.Specification;

public class AttenderAnswerSpecs {

    public static Specification<AttenderAnswer> belongsTo(Long attenderStateId) {
        return (root, query, builder) -> {
            Join<AttenderAnswer, AttenderState> attenderState = root.join("attenderState");
            return builder.equal(attenderState.get("id"), attenderStateId);
        };
    }

}
