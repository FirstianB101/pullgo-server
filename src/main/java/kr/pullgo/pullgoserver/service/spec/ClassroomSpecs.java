package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class ClassroomSpecs {

    public static Specification<Classroom> hasStudent(Long studentId) {
        return (root, query, builder) -> {
            Join<Classroom, Student> join = root.joinSet("students");
            return builder.equal(join.get("id"), studentId);
        };
    }

    public static Specification<Classroom> hasApplyingStudent(Long applyingStudentId) {
        return (root, query, builder) -> {
            Join<Classroom, Teacher> join = root.joinSet("applyingStudents");
            return builder.equal(join.get("id"), applyingStudentId);
        };
    }

    public static Specification<Classroom> hasTeacher(Long teacherId) {
        return (root, query, builder) -> {
            Join<Classroom, Teacher> join = root.joinSet("teachers");
            return builder.equal(join.get("id"), teacherId);
        };
    }

    public static Specification<Classroom> hasApplyingTeacher(Long applyingTeacherId) {
        return (root, query, builder) -> {
            Join<Classroom, Teacher> join = root.joinSet("applyingTeachers");
            return builder.equal(join.get("id"), applyingTeacherId);
        };
    }

    public static Specification<Classroom> belongsTo(Long academyId) {
        return (root, query, builder) -> {
            Join<Classroom, Teacher> join = root.join("academy");
            return builder.equal(join.get("id"), academyId);
        };
    }

    public static Specification<Classroom> nameLike(String pattern) {
        return (root, query, builder) -> builder.like(root.get("name"), pattern);
    }

}
