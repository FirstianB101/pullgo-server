package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class TeacherSpecs {

    public static Specification<Teacher> isEnrolledInAcademy(Long academyId) {
        return (root, query, builder) -> {
            Join<Teacher, Academy> join = root.joinSet("academies");
            return builder.equal(join.get("id"), academyId);
        };
    }

    public static Specification<Teacher> hasAppliedToAcademy(Long appliedAcademyId) {
        return (root, query, builder) -> {
            Join<Teacher, Academy> join = root.joinSet("appliedAcademies");
            return builder.equal(join.get("id"), appliedAcademyId);
        };
    }

    public static Specification<Teacher> isEnrolledInClassroom(Long classroomId) {
        return (root, query, builder) -> {
            Join<Teacher, Classroom> join = root.joinSet("classrooms");
            return builder.equal(join.get("id"), classroomId);
        };
    }

    public static Specification<Teacher> hasAppliedToClassroom(Long appliedClassroomId) {
        return (root, query, builder) -> {
            Join<Teacher, Classroom> join = root.joinSet("appliedClassrooms");
            return builder.equal(join.get("id"), appliedClassroomId);
        };
    }

}
