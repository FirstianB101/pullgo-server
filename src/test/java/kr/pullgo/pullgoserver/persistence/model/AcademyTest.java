package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.authorizer.AuthenticationInspector;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import kr.pullgo.pullgoserver.service.exam.ExamCronJobService;
import kr.pullgo.pullgoserver.service.exam.ExamFinishService;
import kr.pullgo.pullgoserver.service.exam.OnGoingExamFindService;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class, ExamFinishService.class, ExamCronJobService.class,
    OnGoingExamFindService.class, ExamAuthorizer.class, ServiceErrorHelper.class,
    RepositoryHelper.class, AuthenticationInspector.class, ServiceErrorHelper.class})
class AcademyTest {

    @Autowired
    private EntityHelper entityHelper;

    @Nested
    class AcceptStudent {

        @Test
        void acceptStudent() {
            // Given
            Academy academy = entityHelper.generateAcademy();
            Student student = entityHelper.generateStudent(it -> {
                it.applyAcademy(academy);
                return it;
            });

            // When
            academy.acceptStudent(student);

            // Then
            assertThat(academy.getStudents())
                .contains(student);
            assertThat(academy.getApplyingStudents())
                .doesNotContain(student);
        }

        @Test
        void acceptStudent_NotApplied_ExceptionThrown() {
            // Given
            Academy academy = entityHelper.generateAcademy();
            Student student = entityHelper.generateStudent();

            // When
            Throwable thrown = catchThrowable(() -> academy.acceptStudent(student));

            // Then
            assertThat(thrown).isInstanceOf(StudentNotFoundException.class);
        }
    }

    @Nested
    class AcceptTeacher {

        @Test
        void acceptTeacher() {
            // Given
            Academy academy = entityHelper.generateAcademy();
            Teacher teacher = entityHelper.generateTeacher(it -> {
                it.applyAcademy(academy);
                return it;
            });

            // When
            academy.acceptTeacher(teacher);

            // Then
            assertThat(academy.getTeachers())
                .contains(teacher);
            assertThat(academy.getApplyingTeachers())
                .doesNotContain(teacher);
        }

        @Test
        void acceptTeacher_NotApplied_ExceptionThrown() {
            // Given
            Academy academy = entityHelper.generateAcademy();
            Teacher teacher = entityHelper.generateTeacher();

            // When
            Throwable thrown = catchThrowable(() -> academy.acceptTeacher(teacher));

            // Then
            assertThat(thrown).isInstanceOf(TeacherNotFoundException.class);
        }
    }

}