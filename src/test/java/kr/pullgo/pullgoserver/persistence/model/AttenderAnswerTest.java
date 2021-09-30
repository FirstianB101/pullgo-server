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
public class AttenderAnswerTest {

    @Autowired
    private EntityHelper entityHelper;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void withTest() {
        //given
        AttenderState attenderState = entityHelper.generateAttenderState();

        //when
        AttenderAnswer attenderAnswer = entityHelper.generateAttenderAnswer(it -> it.withAttenderState(attenderState));

        //then
        assertThat(attenderAnswer.getAttenderState()).isEqualTo(attenderState);
        assertThat(attenderState.getAnswers()).contains(attenderAnswer);
    }
}
