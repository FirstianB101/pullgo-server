package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StudentTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ExamRepository examRepository;

    @Autowired
    AttenderStateRepository attenderStateRepository;

    @Autowired
    EntityManager em;

    @Test
    void deleteExam_StudentAttendedExam_StudentAttendingStatesUpdated() {
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
        Exam exam = examRepository.save(
            Exam.builder()
                .name("My Exam")
                .beginDateTime(LocalDateTime.of(2020, 1, 1, 0, 0))
                .endDateTime(LocalDateTime.of(2020, 1, 2, 0, 0))
                .timeLimit(Duration.ZERO)
                .passScore(100)
                .build()
        );

        AttenderState attenderState = attenderStateRepository.save(new AttenderState());
        attenderState.setAttender(student);
        attenderState.setExam(exam);
        attenderStateRepository.flush();

        examRepository.delete(exam);
        examRepository.flush();
        em.refresh(student);

        assertThat(examRepository.findAll()).isEmpty();
        assertThat(attenderStateRepository.findAll()).isEmpty();
        assertThat(student.getAttendingStates()).isEmpty();
    }
}
