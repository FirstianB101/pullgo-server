package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import org.springframework.data.jpa.domain.Specification;

public class StudentSpecs {

    public static Specification<Student> isEnrolledInAcademy(Long academyId) {
        return (root, query, builder) -> {
            Join<Student, Academy> join = root.joinSet("academies");
            return builder.equal(join.get("id"), academyId);
        };
    }

    public static Specification<Student> hasAppliedToAcademy(Long appliedAcademyId) {
        return (root, query, builder) -> {
            Join<Student, Academy> join = root.joinSet("appliedAcademies");
            return builder.equal(join.get("id"), appliedAcademyId);
        };
    }

    public static Specification<Student> isEnrolledInClassroom(Long classroomId) {
        return (root, query, builder) -> {
            Join<Student, Classroom> join = root.joinSet("classrooms");
            return builder.equal(join.get("id"), classroomId);
        };
    }

    public static Specification<Student> hasAppliedToClassroom(Long appliedClassroomId) {
        return (root, query, builder) -> {
            Join<Student, Classroom> join = root.joinSet("appliedClassrooms");
            return builder.equal(join.get("id"), appliedClassroomId);
        };
    }

}
