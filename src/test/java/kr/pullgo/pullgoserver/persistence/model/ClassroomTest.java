package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ClassroomTest {

    @Autowired
    ClassroomRepository classroomRepository;

    @Autowired
    LessonRepository lessonRepository;

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
}