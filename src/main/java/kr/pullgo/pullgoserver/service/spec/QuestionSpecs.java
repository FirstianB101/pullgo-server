package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import org.springframework.data.jpa.domain.Specification;

public class QuestionSpecs {

    public static Specification<Question> belongsTo(Long examId) {
        return (root, query, builder) -> {
            Join<Question, Exam> exam = root.join("exam");
            return builder.equal(exam.get("id"), examId);
        };
    }

}
