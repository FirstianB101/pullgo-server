package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(EntityHelper.class)
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
