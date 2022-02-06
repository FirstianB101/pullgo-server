package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.authorizer.AuthenticationInspector;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import kr.pullgo.pullgoserver.service.exam.ExamManagement;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class, ExamManagement.class, ExamAuthorizer.class, ServiceErrorHelper.class,
    RepositoryHelper.class, AuthenticationInspector.class, ServiceErrorHelper.class})
public class AttenderAnswerTest {

    @Autowired
    private EntityHelper entityHelper;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void generateAnswerWithAttenderState_AlreadyExistedAttenderState_CoexistedEntityMapping() {
        //given
        AttenderState attenderState = entityHelper.generateAttenderState();

        //when
        AttenderAnswer attenderAnswer = entityHelper.generateAttenderAnswer(it -> it.withAttenderState(attenderState));

        //then
        assertThat(attenderAnswer.getAttenderState()).isEqualTo(attenderState);
        assertThat(attenderState.getAnswers()).contains(attenderAnswer);
    }
}
