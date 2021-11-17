package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.helper.AuthHelper;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AttenderStateIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("응시상태 ID");
    private static final FieldDescriptor DOC_FIELD_ATTENDER_ID =
        fieldWithPath("attenderId").description("시험 응시자 이름");
    private static final FieldDescriptor DOC_FIELD_EXAM_ID =
        fieldWithPath("examId").description("시험 ID");
    private static final FieldDescriptor DOC_PROGRESS =
        fieldWithPath("progress").description("시험 응시 진행상태");
    private static final FieldDescriptor DOC_SCORE =
        fieldWithPath("score").description("시험 점수 (정답 백분률)");

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

    @Autowired
    private AuthHelper authHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
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

            // Document
            actions.andDo(document("attenderState-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_ATTENDER_ID,
                    DOC_FIELD_EXAM_ID,
                    DOC_PROGRESS,
                    DOC_SCORE.optional()
                )));
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

            // Document
            actions.andDo(document("attenderState-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
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

            // Document
            actions.andDo(document("attenderState-search-example",
                requestParameters(
                    parameterWithName("examId")
                        .description("응시중인 시험 ID").optional(),
                    parameterWithName("studentId")
                        .description("시험 응시생 ID").optional()
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
            Student attender = entityHelper.generateStudent();
            Exam exam = entityHelper.generateExam();
            String token = authHelper.generateToken(it -> attender.getAccount());
            return new Struct()
                .withValue("token", token)
                .withValue("examId", exam.getId())
                .withValue("attenderId", attender.getId());
        });
        String token = given.valueOf("token");
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
            .header("Authorization", "Bearer " + token)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.attenderId").value(attenderId))
            .andExpect(jsonPath("$.examId").value(examId))
            .andExpect(jsonPath("$.progress").value(AttendingProgress.ONGOING.toString()))
            .andExpect(jsonPath("$.score").value(nullValue()));

        // Document
        actions.andDo(document("attenderState-create-example",
            requestFields(
                DOC_FIELD_ATTENDER_ID,
                DOC_FIELD_EXAM_ID
            )));
    }

    @Nested
    class SubmitAttenderState {

        @Test
        void submitAttenderState() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Question question1 = entityHelper.generateQuestion(
                    it -> it.withAnswer(new Answer(5)));
                Question question2 = entityHelper.generateQuestion(
                    it -> it.withAnswer(new Answer(1)));
                Question question3 = entityHelper.generateQuestion(
                    it -> it.withAnswer(new Answer(2)));
                Exam exam = entityHelper.generateExam(it ->
                    it.withTimeLimit(Duration.ofHours(1))
                        .withBeginDateTime(LocalDateTime.now().minusHours(3))
                        .withEndDateTime(LocalDateTime.now().plusHours(3))
                        .withQuestions(Set.of(question1, question2, question3))
                );

                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExamStartTime(LocalDateTime.now())
                        .withExam(exam)
                );
                attenderState.addAnswer(entityHelper.generateAttenderAnswer(at
                    -> at.withQuestion(question2).withAnswer(new Answer(2))));
                attenderState.addAnswer(entityHelper.generateAttenderAnswer(at
                    -> at.withQuestion(question3).withAnswer(new Answer(1))));
                attenderState.addAnswer(entityHelper.generateAttenderAnswer(at
                    -> at.withQuestion(question1).withAnswer(new Answer(5))));

                String token = authHelper.generateToken(
                    it -> attenderState.getAttender().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("attenderId", attenderState.getAttender().getId())
                    .withValue("examId", attenderState.getExam().getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attenderStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            AttenderState resultAttenderState = attenderStateRepository.findById(attenderStateId)
                .orElseThrow();

            assertThat(resultAttenderState.getProgress()).isEqualTo(AttendingProgress.COMPLETE);
            assertThat(resultAttenderState.getScore()).isEqualTo(33);

            // Document
            actions.andDo(document("attenderState-submit-example"));
        }

        @Test
        void question이_없는_Eaxm에_submit() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withTimeLimit(Duration.ofHours(1))
                        .withBeginDateTime(LocalDateTime.now().minusHours(3))
                        .withEndDateTime(LocalDateTime.now().plusHours(3))
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExamStartTime(LocalDateTime.now())
                        .withExam(exam)
                );
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("attenderId", attenderState.getAttender().getId())
                    .withValue("examId", attenderState.getExam().getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attenderStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isBadRequest())
                .andExpect(content().string(emptyString()));

        }

        @Test
        void submitAttenderState_AttenderStateNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(post("/exam/attender-states/{id}/submit", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void submitAttenderState_BadAttendingProgress_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withProgress(AttendingProgress.COMPLETE)
                );
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attendeStateId", attenderState.getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attendeStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AfterTimeLimit_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withTimeLimit(Duration.ofHours(1))
                        .withBeginDateTime(LocalDateTime.now().minusHours(3))
                        .withEndDateTime(LocalDateTime.now().plusHours(3))
                        .withQuestions(Set.of(entityHelper.generateQuestion()))
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExamStartTime(LocalDateTime.now().minusHours(2))
                        .withExam(exam)
                );
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attendeStateId", attenderState.getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attendeStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AlreadyFinishedExam_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withFinished(true)
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attendeStateId", attenderState.getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attendeStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withCancelled(true)
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                );
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attendeStateId", attenderState.getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attendeStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void submitAttenderState_AfterTimeRange_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withTimeLimit(Duration.ofHours(1))
                        .withBeginDateTime(LocalDateTime.now().minusHours(3))
                        .withEndDateTime(LocalDateTime.now().plusHours(3))
                );
                AttenderState attenderState = entityHelper.generateAttenderState(it ->
                    it.withExam(exam)
                        .withExamStartTime(LocalDateTime.now().minusHours(2))
                );
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attendeStateId", attenderState.getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attendeStateId");

            // When
            ResultActions actions = mockMvc
                .perform(post("/exam/attender-states/{id}/submit", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

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
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState();
                Student attender = attenderState.getAttender();
                String token = authHelper.generateToken(it -> attender.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attendeStateId", attenderState.getId());
            });
            String token = given.valueOf("token");
            Long attenderStateId = given.valueOf("attendeStateId");

            // When
            ResultActions actions = mockMvc.perform(
                delete("/exam/attender-states/{id}", attenderStateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(attenderStateRepository.findById(attenderStateId)).isEmpty();

            // Document
            actions.andDo(document("attenderState-delete-example"));
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
