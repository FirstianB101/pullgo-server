package kr.pullgo.pullgoserver.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import org.junit.jupiter.api.Test;

class AttenderStateDtoMapperTest {

    private final AttenderStateDtoMapper dtoMapper = new AttenderStateDtoMapper();

    @Test
    void asEntity() {
        // When
        AttenderStateDto.Create dto = AttenderStateDto.Create.builder()
            .attenderId(0L)
            .examId(0L)
            .build();

        AttenderState entity = dtoMapper.asEntity(dto);

        // Then
        assertThat(entity).isNotNull();
    }

    @Test
    void asResultDto() {
        // When
        AttenderState entity = new AttenderState();
        entity.setId(0L);
        entity.setAttender(studentWithId(1L));
        entity.setExam(examWithId(2L));
        entity.setProgress(AttendingProgress.COMPLETE);
        entity.setScore(100);
        entity.setSubmitted(true);

        AttenderStateDto.Result dto = dtoMapper.asResultDto(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getAttenderId()).isEqualTo(1L);
        assertThat(dto.getExamId()).isEqualTo(2L);
        assertThat(dto.getProgress()).isEqualTo(AttendingProgress.COMPLETE);
        assertThat(dto.getScore()).isEqualTo(100);
        assertThat(dto.getSubmitted()).isEqualTo(true);
    }

    private Student studentWithId(Long id) {
        Student student = Student.builder()
            .parentPhone("01000000000")
            .schoolName("asdf")
            .schoolYear(1)
            .build();
        student.setId(id);
        Account account = Account.builder()
            .username("JottsungE")
            .fullName("Kim eun seong")
            .password("mincho")
            .build();
        student.setAccount(account);
        return student;
    }

    private Exam examWithId(Long id) {
        Exam exam = Exam.builder()
            .name("Test")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ZERO)
            .build();
        exam.setId(id);
        return exam;
    }
}