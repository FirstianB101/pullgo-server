package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(EntityHelper.class)
public class QuestionTest {

    @Autowired
    private EntityHelper entityHelper;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void generateQuestionWithExam_AlreadyExistedExam_CoexistedEntityMapping() {
        //given
        Exam exam = entityHelper.generateExam();

        //when
        Question question = entityHelper.generateQuestion(it -> it.withExam(exam));

        //then
        assertThat(question.getExam()).isEqualTo(exam);
        assertThat(exam.getQuestions()).contains(question);
    }
}
