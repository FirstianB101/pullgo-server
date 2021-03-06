package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.helper.AttenderStateHelper.anAttenderStateUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.dto.AttenderStateDto.Update;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
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
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
    }

    @Nested
    class GetAttenderState {

        @Test
        void getAttenderState() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState();

                return new Struct()
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("attenderId", attenderState.getAttender().getId())
                    .withValue("examId", attenderState.getExam().getId());
            });
            Long attenderStateId = given.valueOf("attenderStateId");
            Long attenderId = given.valueOf("attenderId");
            Long examId = given.valueOf("examId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attenderStateId))
                .andExpect(jsonPath("$.attenderId").value(attenderId))
                .andExpect(jsonPath("$.examId").value(examId))
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
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderStateA = entityHelper.generateAttenderState();
                AttenderState attenderStateB = entityHelper.generateAttenderState();

                return new Struct()
                    .withValue("attenderStateAId", attenderStateA.getId())
                    .withValue("attenderStateBId", attenderStateB.getId());
            });
            Long attenderStateAId = given.valueOf("attenderStateAId");
            Long attenderStateBId = given.valueOf("attenderStateBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderStateAId.intValue(),
                    attenderStateBId.intValue()
                )));
        }

        @Test
        void listAttenderStatesWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateAttenderState();
                AttenderState attenderStateA = entityHelper.generateAttenderState();
                AttenderState attenderStateB = entityHelper.generateAttenderState();

                return new Struct()
                    .withValue("attenderStateAId", attenderStateA.getId())
                    .withValue("attenderStateBId", attenderStateB.getId());
            });
            Long attenderStateAId = given.valueOf("attenderStateAId");
            Long attenderStateBId = given.valueOf("attenderStateBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    attenderStateBId.intValue(),
                    attenderStateAId.intValue()
                )));
        }

        @Test
        void searchAttenderStatesByStudentId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();

                AttenderState attenderStateA = entityHelper.generateAttenderState(it ->
                    it.withAttender(student)
                );
                AttenderState attenderStateB = entityHelper.generateAttenderState(it ->
                    it.withAttender(student)
                );
                entityHelper.generateAttenderState();

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("attenderStateAId", attenderStateA.getId())
                    .withValue("attenderStateBId", attenderStateB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long attenderStateAId = given.valueOf("attenderStateAId");
            Long attenderStateBId = given.valueOf("attenderStateBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states")
                .param("studentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderStateAId.intValue(),
                    attenderStateBId.intValue()
                )));
        }

        @Test
        void searchAttenderStatesByExamId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();

                AttenderState attenderStateA = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );
                AttenderState attenderStateB = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );
                entityHelper.generateAttenderState();

                return new Struct()
                    .withValue("examId", exam.getId())
                    .withValue("attenderStateAId", attenderStateA.getId())
                    .withValue("attenderStateBId", attenderStateB.getId());
            });
            Long examId = given.valueOf("examId");
            Long attenderStateAId = given.valueOf("attenderStateAId");
            Long attenderStateBId = given.valueOf("attenderStateBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-states")
                .param("examId", examId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderStateAId.intValue(),
                    attenderStateBId.intValue()
                )));
        }

    }

    @Test
    void postAttenderState() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Student attenderId = entityHelper.generateStudent();
            Exam exam = entityHelper.generateExam();

            return new Struct()
                .withValue("examId", exam.getId())
                .withValue("attenderId", attenderId.getId());
        });
        Long attenderId = given.valueOf("attenderId");
        Long examId = given.valueOf("examId");

        // When
        AttenderStateDto.Create dto = AttenderStateDto.Create.builder()
            .attenderId(attenderId)
            .examId(examId)
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/exam/attender-states")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.attenderId").value(attenderId))
            .andExpect(jsonPath("$.examId").value(examId))
            .andExpect(jsonPath("$.progress").value(AttendingProgress.ONGOING.toString()))
            .andExpect(jsonPath("$.score").value(nullValue()));
    }

    @Nested
    class PatchAttenderState {

        @Test
        void patchAttenderState() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState();

                return new Struct()
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("attenderId", attenderState.getAttender().getId())
                    .withValue("examId", attenderState.getExam().getId());
            });
            Long attenderStateId = given.valueOf("attenderStateId");
            Long attenderId = given.valueOf("attenderId");
            Long examId = given.valueOf("examId");

            // When
            Update dto = Update.builder()
                .progress(AttendingProgress.COMPLETE)
                .score(85)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exam/attender-states/{id}", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.attenderId").value(attenderId))
                .andExpect(jsonPath("$.examId").value(examId))
                .andExpect(jsonPath("$.progress").value(AttendingProgress.COMPLETE.toString()))
                .andExpect(jsonPath("$.score").value(85));
        }

        @Test
        void patchAttenderState_AttenderStateNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAttenderStateUpdateDto());

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
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withTimeLimit(Duration.ofHours(1))
                        .withBeginDateTime(LocalDateTime.of(2021, 1, 11, 0, 0))
                        .withEndDateTime(LocalDateTime.of(2021, 1, 13, 0, 0))
                );

                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExamStartTime(LocalDateTime.of(2021, 1, 12, 0, 0))
                        .withExam(exam)
                );

                return new Struct()
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("attenderId", attenderState.getAttender().getId())
                    .withValue("examId", attenderState.getExam().getId());
            });
            Long attenderStateId = given.valueOf("attenderStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            AttenderState resultAttenderState = attenderStateRepository.findById(attenderStateId)
                .orElseThrow();

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
            Long attenderStateId = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withProgress(AttendingProgress.COMPLETE)
                );
                return attenderState.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AfterTimeLimit_BadRequestStatus() throws Exception {
            // Given
            Long attenderStateId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withTimeLimit(Duration.ofHours(1))
                        .withBeginDateTime(LocalDateTime.of(2021, 1, 9, 0, 0))
                        .withEndDateTime(LocalDateTime.of(2021, 1, 10, 0, 0))
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                        .withProgress(AttendingProgress.COMPLETE)
                );

                return attenderState.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AlreadyFinishedExam_BadRequestStatus() throws Exception {
            // Given
            Long attenderStateId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withFinished(true)
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );

                return attenderState.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            Long attenderStateId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withCancelled(true)
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );

                return attenderState.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AfterTimeRange_BadRequestStatus() throws Exception {
            // Given
            Long attenderStateId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withBeginDateTime(LocalDateTime.of(2021, 1, 9, 0, 0))
                        .withEndDateTime(LocalDateTime.of(2021, 1, 10, 0, 0))
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );

                return attenderState.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}", attenderStateId));

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
            Long attenderStateId = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState();
                return attenderState.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/attender-states/{id}", attenderStateId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(attenderStateRepository.findById(attenderStateId)).isEmpty();
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

}
