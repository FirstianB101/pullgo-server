package kr.pullgo.pullgoserver.service.authorizer;

import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ExamAuthorizer extends AbstractAuthorizer {

    private final AuthenticationInspector authInspector;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public ExamAuthorizer(
        AuthenticationInspector authInspector,
        StudentRepository studentRepository,
        TeacherRepository teacherRepository,
        ServiceErrorHelper errorHelper) {
        super(authInspector, studentRepository, teacherRepository, errorHelper);
        this.authInspector = authInspector;
        this.errorHelper = errorHelper;
    }

    public void requireCreator(Authentication authentication, Exam exam) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        Teacher teacher = getTeacherOrThrow(account);
        if (teacher != exam.getCreator()) {
            throw errorHelper.forbidden("Not the creator");
        }
    }

}
