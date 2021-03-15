package kr.pullgo.pullgoserver.util;

import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
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

    public static ResponseStatusException examAlreadyFinished() {
        return badRequest(Exam.class, "exam already finished");
    }

    public static ResponseStatusException examAlreadyCanceled() {
        return badRequest(Exam.class, "exam already canceled");
    }

    public static ResponseStatusException attenderStateSubmittedAlreadyFinishedExam() {
        return badRequest(Exam.class, "attender state already finished exam");
    }

    public static ResponseStatusException attenderStateSubmittedAlreadyCancelledExam() {
        return badRequest(Exam.class, "attender state already canceled exam");
    }

    public static ResponseStatusException attenderStateSubmittedAfterTimeLimit() {
        return badRequest(AttenderState.class, "attender state submitted after timeout");
    }

    public static ResponseStatusException attenderStateSubmittedAfterTimeRange() {
        return badRequest(AttenderState.class, "attender state submitted after time range");
    }

    public static ResponseStatusException questionNotFound() {
        return notFound(Question.class);
    }

    public static ResponseStatusException attenderStateNotFound() {
        return notFound(AttenderState.class);
    }

    public static ResponseStatusException badRequest(Class<?> resourceClass, String reason) {
        return badRequest(reason + " at " + resourceClass.getName());
    }

    public static ResponseStatusException badRequest(String reasonWithResourceName) {
        String reason = String.format("bad request by %s", reasonWithResourceName);
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    public static ResponseStatusException notFound(Class<?> resourceClass) {
        return notFound(resourceClass.getName());
    }

    public static ResponseStatusException notFound(String resourceName) {
        String reason = String.format("%s id was not found", resourceName);
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }

}
