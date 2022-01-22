package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class})
class AttenderStateTest {

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private EntityHelper entityHelper;

    @Test
    public void generateAttenderStateWithExamAndAttender_AlreadyExistedExamAndAttender_CoexistedEntityMapping() {
        //given
        Exam exam = entityHelper.generateExam();
        Student attender = entityHelper.generateStudent();

        //when
        AttenderState attenderState = entityHelper.generateAttenderState(
            it -> it.withExam(exam).withAttender(attender)
        );

        //then
        assertThat(attenderState.getExam()).isEqualTo(exam);
        assertThat(exam.getAttenderStates()).contains(attenderState);
        assertThat(attenderState.getAttender()).isEqualTo(attender);
        assertThat(attender.getAttendingStates()).contains(attenderState);
    }

    @Test
    void createAttenderState() {
        // Given
        Student student = entityHelper.generateStudent();
        Exam exam = entityHelper.generateExam();

        // When
        AttenderState attenderState = AttenderState.builder()
            .examStartTime(LocalDateTime.now())
            .build();
        attenderState.setAttender(student);
        attenderState.setExam(exam);

        attenderStateRepository.save(attenderState);

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

}