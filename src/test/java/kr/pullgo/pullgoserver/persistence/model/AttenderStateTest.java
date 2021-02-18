package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AttenderStateTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AttenderStateRepository attenderStateRepository;

    @Autowired
    ExamRepository examRepository;

    @Test
    void createAttenderState() {
        // Given
        Student student = createAndSaveStudent();
        Exam exam = createAndSaveExam();

        // When
        AttenderState attenderState = attenderStateRepository.save(new AttenderState());
        attenderState.setAttender(student);
        attenderState.setExam(exam);

        // Then
        assertThat(attenderState.getAttender().getId())
            .isEqualTo(student.getId());
        assertThat(attenderState.getExam().getId())
            .isEqualTo(exam.getId());

        assertThat(student.getAttendingStates())
            .containsOnly(attenderState);
        assertThat(exam.getAttenderStates())
            .containsOnly(attenderState);
    }

    private Student createAndSaveStudent() {
        Account account = accountRepository.save(
            Account.builder()
                .username("JottsungE")
                .fullName("Kim eun seong")
                .password("mincho")
                .build()
        );
        Student student = studentRepository.save(
            Student.builder()
                .parentPhone("01000000000")
                .schoolName("asdf")
                .schoolYear(1)
                .build()
        );
        student.setAccount(account);
        return student;
    }

    private Exam createAndSaveExam() {
        return examRepository.save(
            Exam.builder()
                .name("Test")
                .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
                .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
                .timeLimit(Duration.ZERO)
                .build()
        );
    }
}