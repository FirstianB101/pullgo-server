package kr.pullgo.pullgoserver.service.authorizer;

import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.security.core.Authentication;

public class AbstractAuthorizer {

    private final AuthenticationInspector authInspector;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ServiceErrorHelper errorHelper;

    public AbstractAuthorizer(
        AuthenticationInspector authInspector,
        StudentRepository studentRepository,
        TeacherRepository teacherRepository,
        ServiceErrorHelper errorHelper) {
        this.authInspector = authInspector;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.errorHelper = errorHelper;
    }

    public void requireByOneself(Authentication authentication, Student requester) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        Student student = getStudentOrThrow(account);
        if (student != requester) {
            throw errorHelper.forbidden("Not requested oneself");
        }
    }

    public void requireByOneself(Authentication authentication, Teacher requester) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        Teacher teacher = getTeacherOrThrow(account);
        if (teacher != requester) {
            throw errorHelper.forbidden("Not requested oneself");
        }
    }

    protected Student getStudentOrThrow(Account account) {
        Student student = studentRepository.findByAccountId(account.getId());
        if (student == null) {
            throw errorHelper.forbidden("Not a student");
        }
        return student;
    }

    protected boolean isStudent(Account account) {
        Student student = studentRepository.findByAccountId(account.getId());
        return student != null;
    }

    protected Teacher getTeacherOrThrow(Account account) {
        Teacher teacher = teacherRepository.findByAccountId(account.getId());
        if (teacher == null) {
            throw errorHelper.forbidden("Not a teacher");
        }
        return teacher;
    }

    protected boolean isTeacher(Account account) {
        Teacher teacher = teacherRepository.findByAccountId(account.getId());
        return teacher != null;
    }

}
