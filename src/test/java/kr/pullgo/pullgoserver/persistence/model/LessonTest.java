package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class})
public class LessonTest {

    @Autowired
    private EntityHelper entityHelper;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void generateLessonWithClassroom_AlreadyExistedClassroom_CoexistedEntityMapping() {
        //given
        Classroom classroom = entityHelper.generateClassroom();

        //when
        Lesson lesson = entityHelper.generateLesson(it -> it.withClassroom(classroom));

        //then
        assertThat(lesson.getClassroom()).isEqualTo(classroom);
        assertThat(classroom.getLessons()).contains(lesson);
    }
}
