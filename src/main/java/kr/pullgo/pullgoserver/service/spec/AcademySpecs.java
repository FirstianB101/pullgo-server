package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class AcademySpecs {

    public static Specification<Academy> hasStudent(Long studentId) {
        return (root, query, builder) -> {
            Join<Academy, Student> join = root.joinSet("students");
            return builder.equal(join.get("id"), studentId);
        };
    }

    public static Specification<Academy> hasApplyingStudent(Long applyingStudentId) {
        return (root, query, builder) -> {
            Join<Academy, Teacher> join = root.joinSet("applyingStudents");
            return builder.equal(join.get("id"), applyingStudentId);
        };
    }

    public static Specification<Academy> hasTeacher(Long teacherId) {
        return (root, query, builder) -> {
            Join<Academy, Teacher> join = root.joinSet("teachers");
            return builder.equal(join.get("id"), teacherId);
        };
    }

    public static Specification<Academy> hasApplyingTeacher(Long applyingTeacherId) {
        return (root, query, builder) -> {
            Join<Academy, Teacher> join = root.joinSet("applyingTeachers");
            return builder.equal(join.get("id"), applyingTeacherId);
        };
    }

    public static Specification<Academy> ownerId(Long ownerId) {
        return (root, query, builder) -> {
            Join<Academy, Teacher> join = root.join("owner");
            return builder.equal(join.get("id"), ownerId);
        };
    }

}
