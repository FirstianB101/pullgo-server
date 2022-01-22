package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class})
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
