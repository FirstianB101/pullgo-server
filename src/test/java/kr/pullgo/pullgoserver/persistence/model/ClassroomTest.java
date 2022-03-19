package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.persistence.repository.ScheduleRepository;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.authorizer.AuthenticationInspector;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import kr.pullgo.pullgoserver.service.exam.ExamCronJobService;
import kr.pullgo.pullgoserver.service.exam.ExamFinishService;
import kr.pullgo.pullgoserver.service.exam.OnGoingExamFindService;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class, ExamFinishService.class, ExamCronJobService.class,
    OnGoingExamFindService.class, ExamAuthorizer.class, ServiceErrorHelper.class,
    RepositoryHelper.class, AuthenticationInspector.class, ServiceErrorHelper.class})
class ClassroomTest {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private EntityHelper entityHelper;

    @Test
    public void generateClassroomWithAcademy_AlreadyExistedAcademy_CoexistedEntityMapping() {
        //given
        Academy academy = entityHelper.generateAcademy();

        //when
        Classroom classroom = entityHelper.generateClassroom(it -> it.withAcademy(academy));

        //then
        assertThat(classroom.getAcademy()).isEqualTo(academy);
        assertThat(academy.getClassrooms()).contains(classroom);
    }

    @Test
    void removeLesson() {
        // Given
        Classroom classroom = entityHelper.generateClassroom();
        Lesson lesson = entityHelper.generateLesson(it -> it.withClassroom(classroom));

        classroomRepository.flush();

        // When
        classroom.removeLesson(lesson);

        // Then
        assertThat(lessonRepository.findAll()).isEmpty();
    }

    @Test
    void removeClassroom_LessenAdded_LessonAndScheduleDeleted() {
        // Given
        Lesson lesson = entityHelper.generateLesson();
        Classroom classroom = lesson.getClassroom();
        Lesson lesson2 = entityHelper.generateLesson(it ->
            it.withClassroom(classroom)
        );

        classroomRepository.flush();

        // When
        classroom.setAcademy(null);
        classroomRepository.delete(classroom);

        // Then
        assertThat(lessonRepository.findAll()).isEmpty();
        assertThat(scheduleRepository.findAll()).isEmpty();
    }

}