package kr.pullgo.pullgoserver.persistence.model;

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
    public void withTest() {
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
        classroom.removeThis();
        classroomRepository.delete(classroom);

        // Then
        assertThat(lessonRepository.findAll()).isEmpty();
        assertThat(scheduleRepository.findAll()).isEmpty();
    }

}