package kr.pullgo.pullgoserver.persistence.model;

import static kr.pullgo.pullgoserver.helper.LessonHelper.aLesson;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aSchedule;
import static org.assertj.core.api.Assertions.assertThat;

import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.persistence.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(EntityHelper.class)
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
    void removeLesson() {
        // Given
        Classroom classroom = entityHelper.generateClassroom();
        Lesson lesson = aLesson()
            .withId(null)
            .withClassroom(null)
            .withSchedule(aSchedule().withId(null));

        classroom.addLesson(lesson);
        classroomRepository.flush();

        // When
        classroom.removeLesson(lesson);

        // Then
        assertThat(lessonRepository.findAll()).isEmpty();
    }

    @Test
    void removeClassroom_LessenAdded_LessonAndScheduleDeleted() {
        // Given
        Classroom classroom = entityHelper.generateClassroom();
        Lesson lesson = aLesson()
            .withId(null)
            .withClassroom(null)
            .withSchedule(aSchedule().withId(null));

        classroom.addLesson(lesson);
        classroomRepository.flush();

        // When
        classroomRepository.delete(classroom);

        // Then
        assertThat(lessonRepository.findAll()).isEmpty();
        assertThat(scheduleRepository.findAll()).isEmpty();
    }

}