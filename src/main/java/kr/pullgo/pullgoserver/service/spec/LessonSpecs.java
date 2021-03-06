package kr.pullgo.pullgoserver.service.spec;

import javax.persistence.criteria.Join;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class LessonSpecs {

    public static Specification<Lesson> belongsTo(Long classroomId) {
        return (root, query, builder) -> {
            Join<Lesson, Classroom> classroom = root.join("classroom");
            return builder.equal(classroom.get("id"), classroomId);
        };
    }

    public static Specification<Lesson> isAssignedToStudent(Long studentId) {
        return (root, query, builder) -> {
            Join<Lesson, Classroom> classroom = root.join("classroom");
            Join<Classroom, Student> students = classroom.joinSet("students");
            return builder.equal(students.get("id"), studentId);
        };
    }

    public static Specification<Lesson> isAssignedToTeacher(Long teacherId) {
        return (root, query, builder) -> {
            Join<Lesson, Classroom> classroom = root.join("classroom");
            Join<Classroom, Teacher> teachers = classroom.joinSet("teachers");
            return builder.equal(teachers.get("id"), teacherId);
        };
    }

}
