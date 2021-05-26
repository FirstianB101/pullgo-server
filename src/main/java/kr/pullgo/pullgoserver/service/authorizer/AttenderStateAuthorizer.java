package kr.pullgo.pullgoserver.service.authorizer;

import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AttenderStateAuthorizer extends AbstractAuthorizer {

    private final AuthenticationInspector authInspector;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AttenderStateAuthorizer(
        AuthenticationInspector authInspector,
        StudentRepository studentRepository,
        TeacherRepository teacherRepository,
        ServiceErrorHelper errorHelper) {
        super(authInspector, studentRepository, teacherRepository, errorHelper);
        this.authInspector = authInspector;
        this.errorHelper = errorHelper;
    }

    public void requireOwningAttender(Authentication authentication, AttenderState attenderState) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        Student student = getStudentOrThrow(account);
        if (student != attenderState.getAttender()) {
            throw errorHelper.forbidden("Not the owning attender");
        }
    }

}
