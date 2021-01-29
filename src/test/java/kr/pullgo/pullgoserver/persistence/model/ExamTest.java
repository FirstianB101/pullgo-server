package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ExamTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AttenderStateRepository attenderStateRepository;

    @Autowired
    ExamRepository examRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Test
    void removeQuestion() {
        Exam exam = examRepository.save(
            Exam.builder()
                .name("Test")
                .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
                .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
                .timeLimit(Duration.ZERO)
                .build()
        );

        Question question = questionRepository.save(
            Question.builder()
                .content("Test question")
                .answer(new Answer(1))
                .build()
        );
        exam.addQuestion(question);
        examRepository.flush();

        exam.removeQuestion(question);

        assertThat(exam.getQuestions()).isEmpty();
        assertThat(questionRepository.findAll()).isEmpty();
    }

    @Test
    void removeAttenderState() {
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
                .name("Test")
                .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
                .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
                .timeLimit(Duration.ZERO)
                .build()
        );

        AttenderState attenderState = attenderStateRepository.save(new AttenderState());
        attenderState.setAttender(student);
        attenderState.setExam(exam);

        attenderStateRepository.flush();

        exam.removeAttenderState(attenderState);

        assertThat(exam.getAttenderStates()).isEmpty();
        assertThat(student.getAttendingStates()).isEmpty();
        assertThat(attenderStateRepository.findAll()).isEmpty();
    }
}