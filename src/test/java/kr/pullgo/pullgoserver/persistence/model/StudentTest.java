package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import kr.pullgo.pullgoserver.config.aop.SchedulingConfig;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({EntityHelper.class, JwtService.class, ObjectMapper.class, CronJob.class,
    SchedulingConfig.class})
class StudentTest {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private EntityManager em;

    @Test
    void deleteExam_StudentAttendedExam_StudentAttendingStatesUpdated() {
        // Given
        Exam exam = entityHelper.generateExam();

        AttenderState attenderState = entityHelper.generateAttenderState(it ->
            it.withAttender(entityHelper.generateStudent())
                .withExam(exam)
        );
        Long attenderId = attenderState.getAttender().getId();

        attenderStateRepository.flush();
        em.clear();

        // When
        examRepository.deleteById(exam.getId());
        examRepository.flush();

        // Then
        assertThat(examRepository.findAll()).isEmpty();
        assertThat(attenderStateRepository.findAll()).isEmpty();

        Student student = studentRepository.findById(attenderId).orElseThrow();
        assertThat(student.getAttendingStates()).isEmpty();
    }

}
