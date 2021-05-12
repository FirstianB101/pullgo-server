package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(EntityHelper.class)
class ExamTest {

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EntityHelper entityHelper;

    @PersistenceContext
    private EntityManager em;

    @Test
    void removeQuestion() {
        // Given
        Exam exam = entityHelper.generateExam();
        Question question = entityHelper.generateQuestion();

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
        Student student = entityHelper.generateStudent();
        Exam exam = entityHelper.generateExam();

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

}