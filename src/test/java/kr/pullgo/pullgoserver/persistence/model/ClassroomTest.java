package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.persistence.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ClassroomTest {

    @Autowired
    ClassroomRepository classroomRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    void removeLesson() {
        // Given
        Classroom classroom = createAndSaveClassroom();
        Lesson lesson = createAndSaveLesson();

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
        Classroom classroom = createAndSaveClassroom();
        Lesson lesson = createAndSaveLesson();
        Schedule schedule = createAndSaveSchedule();

        lesson.setSchedule(schedule);
        classroom.addLesson(lesson);
        classroomRepository.flush();

        // When
        classroomRepository.delete(classroom);

        // Then
        assertThat(lessonRepository.findAll()).isEmpty();
        assertThat(scheduleRepository.findAll()).isEmpty();
    }

    private Classroom createAndSaveClassroom() {
        return classroomRepository.save(
            Classroom.builder()
                .name("Test")
                .build()
        );
    }

    private Lesson createAndSaveLesson() {
        return lessonRepository.save(
            Lesson.builder()
                .name("Test")
                .build()
        );
    }

    private Schedule createAndSaveSchedule() {
        return scheduleRepository.save(
            Schedule.builder()
                .date(LocalDate.of(2021, 2, 15))
                .beginTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(17, 0))
                .build()
        );
    }
}