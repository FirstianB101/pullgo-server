package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class})
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
