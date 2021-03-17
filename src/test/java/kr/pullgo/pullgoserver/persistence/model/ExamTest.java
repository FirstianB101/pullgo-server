package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ExamTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void removeQuestion() {
        // Given
        Exam exam = createAndSaveExam();
        Question question = createAndSaveQuestion();

        exam.addQuestion(question);
        examRepository.flush();

        // When
        exam.removeQuestion(question);

        // Then
        assertThat(exam.getQuestions()).isEmpty();
        assertThat(questionRepository.findAll()).isEmpty();
    }

    @Test
    void deleteAttenderState() {
        // When
        Student student = createAndSaveStudent();
        Exam exam = createAndSaveExam();

        AttenderState attenderState = attenderStateRepository
            .save(AttenderState.builder().examStartTime(LocalDateTime.now()).build());
        attenderState.setAttender(student);
        attenderState.setExam(exam);
        attenderStateRepository.flush();

        // When
        attenderStateRepository.delete(attenderState);
        attenderStateRepository.flush();

        em.refresh(exam);
        em.refresh(student);

        // Then
        assertThat(exam.getAttenderStates()).isEmpty();
        assertThat(student.getAttendingStates()).isEmpty();
        assertThat(attenderStateRepository.findAll()).isEmpty();
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

    private Question createAndSaveQuestion() {
        return questionRepository.save(
            Question.builder()
                .content("Test question")
                .answer(new Answer(1))
                .build()
        );
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
}