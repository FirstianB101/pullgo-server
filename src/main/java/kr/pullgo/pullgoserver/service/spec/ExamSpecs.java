package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class ExamSpecs {

    public static Specification<Exam> belongsTo(Long classroomId) {
        return (root, query, builder) -> {
            Join<Exam, Classroom> classroom = root.join("classroom");
            return builder.equal(classroom.get("id"), classroomId);
        };
    }

    public static Specification<Exam> isCreatedBy(Long creatorId) {
        return (root, query, builder) -> {
            Join<Exam, Teacher> teacher = root.join("creator");
            return builder.equal(teacher.get("id"), creatorId);
        };
    }

    public static Specification<Exam> isAssignedTo(Long studentId) {
        return (root, query, builder) -> {
            Join<Exam, Classroom> classroom = root.join("classroom");
            Join<Classroom, Student> students = classroom.joinSet("students");
            return builder.equal(students.get("id"), studentId);
        };
    }

    public static Specification<Exam> isItFinished(Boolean finished) {
        return (root, query, builder) -> builder.equal(root.get("finished"), finished);
    }

    public static Specification<Exam> isItCancelled(Boolean cancelled) {
        return (root, query, builder) -> builder.equal(root.get("cancelled"), cancelled);
    }
}
