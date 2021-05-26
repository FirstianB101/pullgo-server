package kr.pullgo.pullgoserver.service.authorizer;

import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class StudentAuthorizer extends AbstractAuthorizer {

    private final AuthenticationInspector authInspector;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public StudentAuthorizer(
        AuthenticationInspector authInspector,
        StudentRepository studentRepository,
        TeacherRepository teacherRepository,
        ServiceErrorHelper errorHelper) {
        super(authInspector, studentRepository, teacherRepository, errorHelper);
        this.authInspector = authInspector;
        this.errorHelper = errorHelper;
    }

    public void requireByOneselfOrMemberTeacher(Authentication authentication, Student student,
        Academy academy) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (isStudent(account)) {
            requireByOneself(authentication, student);
        } else {
            Teacher teacher = getTeacherOrThrow(account);
            if (!academy.getTeachers().contains(teacher)) {
                throw errorHelper.forbidden("Not a member teacher");
            }
        }
    }

    public void requireByOneselfOrMemberTeacher(Authentication authentication, Student student,
        Classroom classroom) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (isStudent(account)) {
            requireByOneself(authentication, student);
        } else {
            Teacher teacher = getTeacherOrThrow(account);
            if (!classroom.getTeachers().contains(teacher)) {
                throw errorHelper.forbidden("Not a member teacher");
            }
        }
    }

}
