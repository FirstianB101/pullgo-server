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
        Classroom classroom = classroomRepository.save(
            Classroom.builder()
                .name("Test")
                .build()
        );

        Lesson lesson = lessonRepository.save(
            Lesson.builder()
                .name("Test")
                .build()
        );
        classroom.addLesson(lesson);
        classroomRepository.flush();

        classroom.removeLesson(lesson);

        assertThat(lessonRepository.findAll()).isEmpty();
    }

    @Test
    void removeClassroom_LessenAdded_LessonAndScheduleDeleted() {
        Classroom classroom = classroomRepository.save(
            Classroom.builder()
                .name("Test")
                .build()
        );

        Lesson lesson = lessonRepository.save(
            Lesson.builder()
                .name("Test")
                .build()
        );
        Schedule schedule = scheduleRepository.save(
            Schedule.builder()
                .date(LocalDate.of(2021, 2, 15))
                .beginTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(17, 0))
                .build()
        );
        lesson.setSchedule(schedule);

        classroom.addLesson(lesson);
        classroomRepository.flush();

        classroomRepository.delete(classroom);

        assertThat(lessonRepository.findAll()).isEmpty();
        assertThat(scheduleRepository.findAll()).isEmpty();
    }
}