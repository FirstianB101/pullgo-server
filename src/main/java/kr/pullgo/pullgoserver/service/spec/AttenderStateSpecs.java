package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import org.springframework.data.jpa.domain.Specification;

public class AttenderStateSpecs {

    public static Specification<AttenderState> belongsToStudent(Long studentId) {
        return (root, query, builder) -> {
            Join<AttenderState, Student> student = root.join("attender");
            return builder.equal(student.get("id"), studentId);
        };
    }

    public static Specification<AttenderState> belongsToExam(Long examId) {
        return (root, query, builder) -> {
            Join<AttenderState, Exam> exam = root.join("exam");
            return builder.equal(exam.get("id"), examId);
        };
    }

}
