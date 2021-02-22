package kr.pullgo.pullgoserver.util;

import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResponseStatusExceptions {

    public static ResponseStatusException academyNotFound() {
        return notFound(Academy.class);
    }

    public static ResponseStatusException studentNotFound() {
        return notFound(Student.class);
    }

    public static ResponseStatusException teacherNotFound() {
        return notFound(Teacher.class);
    }

    public static ResponseStatusException classroomNotFound() {
        return notFound(Classroom.class);
    }

    public static ResponseStatusException examNotFound() {
        return notFound(Exam.class);
    }

    public static ResponseStatusException attenderStateNotFound() {
        return notFound(AttenderState.class);
    }

    public static ResponseStatusException notFound(Class<?> resourceClass) {
        return notFound(resourceClass.getName());
    }

    public static ResponseStatusException notFound(String resourceName) {
        String reason = String.format("%s id was not found", resourceName);
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }
}
