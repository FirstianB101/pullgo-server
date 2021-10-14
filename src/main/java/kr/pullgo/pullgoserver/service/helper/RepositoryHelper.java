package kr.pullgo.pullgoserver.service.helper;

import javax.persistence.EntityManager;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RepositoryHelper {

    private final EntityManager em;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public RepositoryHelper(EntityManager em,
        ServiceErrorHelper errorHelper) {
        this.em = em;
        this.errorHelper = errorHelper;
    }

    public <T> T findOrThrow(Class<T> clazz, Object primaryKey) throws ResponseStatusException {
        T entity = em.find(clazz, primaryKey);
        if (entity == null) {
            String reason = String.format("%s id was not found", clazz.getName());
            throw errorHelper.notFound(reason);
        }
        return entity;
    }

    public Account findAccountOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Account.class, id);
    }

    public Student findStudentOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Student.class, id);
    }

    public Teacher findTeacherOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Teacher.class, id);
    }

    public Academy findAcademyOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Academy.class, id);
    }

    public Classroom findClassroomOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Classroom.class, id);
    }

    public Lesson findLessonOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Lesson.class, id);
    }

    public Exam findExamOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Exam.class, id);
    }

    public Question findQuestionOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Question.class, id);
    }

    public AttenderState findAttenderStateOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(AttenderState.class, id);
    }

    public AttenderAnswer findAttenderAnswerOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(AttenderAnswer.class, id);
    }

    public AttenderAnswer findAttenderAnswerOrThrow(Long attenderStateId, Long questionId)
        throws ResponseStatusException {
        AttenderAnswer attenderAnswer = findAttenderStateOrThrow(attenderStateId)
            .getAnswers().stream().filter(it -> it.getQuestion().getId().equals(questionId))
            .findAny().orElseThrow(() -> errorHelper.notFound("attender answer was not found"));

        return findOrThrow(AttenderAnswer.class, attenderAnswer.getId());
    }
}
