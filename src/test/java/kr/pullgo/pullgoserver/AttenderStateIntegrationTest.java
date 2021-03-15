package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.dto.AttenderStateDto.Update;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class AttenderStateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
    }

    @Nested
    class GetAttenderState {

        @Test
        void getAttenderState() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            Student attender = createAndSaveStudent();
            AttenderState attenderState = new AttenderState();
            attenderState.setProgress(AttendingProgress.ONGOING);
            attenderState.setExam(exam);
            attenderState.setAttender(attender);

            examRepository.save(exam);
            studentRepository.save(attender);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attenderState.getId()))
                .andExpect(jsonPath("$.attenderId").value(attender.getId()))
                .andExpect(jsonPath("$.examId").value(exam.getId()))
                .andExpect(jsonPath("$.progress").value(AttendingProgress.ONGOING.toString()))
                .andExpect(jsonPath("$.score").value(nullValue()));
        }

        @Test
        void getAttenderState_AttenderStateNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchAttenderStates {

        @Test
        void listAttenderStates() throws Exception {
            // Given
            AttenderState attenderStateA = createAndSaveAttenderState();
            AttenderState attenderStateB = createAndSaveAttenderState();

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderStateA.getId().intValue(),
                    attenderStateB.getId().intValue()
                )));
        }

        @Test
        void searchAttenderStatesByStudentId() throws Exception {
            // Given
            Student student = createAndSaveStudent();

            AttenderState attenderStateA = createAndSaveAttenderStateWithAttender(student);
            AttenderState attenderStateB = createAndSaveAttenderStateWithAttender(student);
            createAndSaveAttenderState();

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states")
                .param("studentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderStateA.getId().intValue(),
                    attenderStateB.getId().intValue()
                )));
        }

        @Test
        void searchAttenderStatesByExamId() throws Exception {
            // Given
            Exam exam = createAndSaveExam();

            AttenderState attenderStateA = createAndSaveAttenderStateWithExam(exam);
            AttenderState attenderStateB = createAndSaveAttenderStateWithExam(exam);
            createAndSaveAttenderState();

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states")
                .param("examId", exam.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderStateA.getId().intValue(),
                    attenderStateB.getId().intValue()
                )));
        }

    }

    @Test
    void postAttenderState() throws Exception {
        // Given
        Exam exam = createAndSaveExam();
        Student attender = createAndSaveStudent();

        // When
        AttenderStateDto.Create dto = AttenderStateDto.Create.builder()
            .attenderId(attender.getId())
            .examId(exam.getId())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/exam/attender-states")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.attenderId").value(attender.getId()))
            .andExpect(jsonPath("$.examId").value(exam.getId()))
            .andExpect(jsonPath("$.progress").value(AttendingProgress.ONGOING.toString()))
            .andExpect(jsonPath("$.score").value(nullValue()));
    }

    @Nested
    class PatchAttenderState {

        @Test
        void patchAttenderState() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            Student attender = createAndSaveStudent();
            AttenderState attenderState = new AttenderState();
            attenderState.setProgress(AttendingProgress.ONGOING);
            attenderState.setExam(exam);
            attenderState.setAttender(attender);

            examRepository.save(exam);
            studentRepository.save(attender);
            attenderStateRepository.save(attenderState);

            // When
            Update dto = Update.builder()
                .progress(AttendingProgress.COMPLETE)
                .score(85)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exam/attender-states/{id}", attenderState.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.attenderId").value(attender.getId()))
                .andExpect(jsonPath("$.examId").value(exam.getId()))
                .andExpect(jsonPath("$.progress").value(AttendingProgress.COMPLETE.toString()))
                .andExpect(jsonPath("$.score").value(85));
        }

        @Test
        void patchAttenderState_AttenderStateNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(attenderStateUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/exam/attender-states/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SubmitAttenderState {

        @Test
        void submitAttenderState() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            Student attender = createAndSaveStudent();
            AttenderState attenderState = new AttenderState();
            attenderState.setExam(exam);
            attenderState.setAttender(attender);
            attenderState.setProgress(AttendingProgress.ONGOING);
            attenderState.setExamStartTime(stringToLocalDateTime("2021-01-12T00:00:00"));
            exam.setTimeLimit(stringToDuration("PT1H"));
            exam.setBeginDateTime(stringToLocalDateTime("2021-01-11T00:00:00"));
            exam.setEndDateTime(stringToLocalDateTime("2021-01-13T00:00:00"));

            examRepository.save(exam);
            studentRepository.save(attender);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            AttenderState resultAttenderState = findAttenderStateById(attenderState);

            assertThat(resultAttenderState.getProgress()).isEqualTo(AttendingProgress.COMPLETE);
        }

        @Test
        void submitAttenderState_AttenderStateNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(post("/exam/attender-states/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void submitAttenderState_BadAttendingProgress_BadRequestStatus() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();
            attenderState.setProgress(AttendingProgress.COMPLETE);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AfterTimeLimit_BadRequestStatus() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();
            Exam exam = attenderState.getExam();
            exam.setTimeLimit(stringToDuration("PT1H"));
            exam.setBeginDateTime(stringToLocalDateTime("2021-01-09T00:00:00"));
            exam.setEndDateTime(stringToLocalDateTime("2021-01-10T00:00:00"));
            attenderState.setExamStartTime(stringToLocalDateTime("2021-01-12T00:00:00"));

            examRepository.save(exam);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AlreadyFinishedExam_BadRequestStatus() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();
            Exam exam = attenderState.getExam();
            exam.setFinished(true);

            examRepository.save(exam);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();
            Exam exam = attenderState.getExam();
            exam.setCancelled(true);

            examRepository.save(exam);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AfterTimeRange_BadRequestStatus() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();
            Exam exam = attenderState.getExam();
            exam.setBeginDateTime(stringToLocalDateTime("2021-01-09T00:00:00"));
            exam.setEndDateTime(stringToLocalDateTime("2021-01-10T00:00:00"));

            examRepository.save(exam);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DeleteAttenderState {

        @Test
        void deleteAttenderState() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/attender-states/{id}", attenderState.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(attenderStateRepository.findById(attenderState.getId())).isEmpty();
        }

        @Test
        void deleteAttenderState_AttenderStateNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/exam/attender-states/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private AttenderState findAttenderStateById(AttenderState attenderState) {
        return attenderStateRepository
            .findById(attenderState.getId()).orElseThrow();
    }

    private Duration stringToDuration(String duration) {
        return Duration.parse(duration);
    }

    private LocalDateTime stringToLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

    private Exam createAndSaveExam() {
        return examRepository.save(Exam.builder()
            .name("test exam")
            .timeLimit(stringToDuration("PT1H"))
            .beginDateTime(stringToLocalDateTime("2021-01-11T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-01-13T00:00:00"))
            .build());
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


    private AttenderState createAndSaveAttenderState() {
        Exam exam = createAndSaveExam();
        Student attender = createAndSaveStudent();
        AttenderState attenderState = createAttenderState(exam, attender);

        examRepository.save(exam);
        studentRepository.save(attender);
        attenderStateRepository.save(attenderState);
        return attenderState;
    }

    private AttenderState createAttenderState(Exam exam, Student attender) {
        AttenderState attenderState = new AttenderState();
        attenderState.setExamStartTime(stringToLocalDateTime("2021-01-12T00:00:00"));
        attenderState.setProgress(AttendingProgress.ONGOING);
        attenderState.setExam(exam);
        attenderState.setAttender(attender);
        return attenderState;
    }

    private AttenderState createAndSaveAttenderStateWithAttender(Student attender) {
        Exam exam = createAndSaveExam();
        AttenderState attenderState = createAttenderState(exam, attender);

        examRepository.save(exam);
        attenderStateRepository.save(attenderState);
        return attenderState;
    }

    private AttenderState createAndSaveAttenderStateWithExam(Exam exam) {
        Student attender = createAndSaveStudent();
        AttenderState attenderState = createAttenderState(exam, attender);

        studentRepository.save(attender);
        attenderStateRepository.save(attenderState);
        return attenderState;
    }

    private Update attenderStateUpdateDto() {
        return Update.builder()
            .progress(AttendingProgress.COMPLETE)
            .score(85)
            .build();
    }
}
