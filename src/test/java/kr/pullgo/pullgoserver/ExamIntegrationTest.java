package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.ExamHelper.anExamUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.ExamDto.Update;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import kr.pullgo.pullgoserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ExamIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("시험 ID");
    private static final FieldDescriptor DOC_FIELD_CLASSROOM_ID =
        fieldWithPath("classroomId").description("소속된 반 ID");
    private static final FieldDescriptor DOC_FIELD_CREATOR_ID =
        fieldWithPath("creatorId").description("생성한 선생님 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("시험 이름");
    private static final FieldDescriptor DOC_FIELD_BEGIN_DATE_TIME =
        fieldWithPath("beginDateTime").description("시험 시작 일시");
    private static final FieldDescriptor DOC_FIELD_END_DATE_TIME =
        fieldWithPath("endDateTime").description("시험 종료 일시");
    private static final FieldDescriptor DOC_FIELD_TIME_LIMIT =
        fieldWithPath("timeLimit").description("응시 시간");
    private static final FieldDescriptor DOC_FIELD_PASS_SCORE =
        fieldWithPath("passScore").description("기준 점수");
    private static final FieldDescriptor DOC_FIELD_CANCELLED =
        fieldWithPath("cancelled").description("시험 취소 여부");
    private static final FieldDescriptor DOC_FIELD_FINISHED =
        fieldWithPath("finished").description("시험 종료 여부");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @MockBean
    private CronJob cronJob;

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
    class GetExam {

        @Test
        void getExam() throws Exception {
            // Given
            doNothing().when(cronJob).register(
                anyLong(), any(Runnable.class),
                any(LocalDateTime.class), anyString());

            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withName("3월 모의고사")
                        .withBeginDateTime(LocalDateTime.of(2021, 3, 2, 0, 0))
                        .withEndDateTime(LocalDateTime.of(2021, 3, 4, 12, 0))
                        .withTimeLimit(Duration.ofHours(1))
                        .withPassScore(70)
                );

                return new Struct()
                    .withValue("examId", exam.getId())
                    .withValue("classroomId", exam.getClassroom().getId())
                    .withValue("creatorId", exam.getCreator().getId());
            });
            Long examId = given.valueOf("examId");
            Long classroomId = given.valueOf("classroomId");
            Long creatorId = given.valueOf("creatorId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/exams/{id}", examId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(examId))
                .andExpect(jsonPath("$.classroomId").value(classroomId))
                .andExpect(jsonPath("$.creatorId").value(creatorId))
                .andExpect(jsonPath("$.name").value("3월 모의고사"))
                .andExpect(jsonPath("$.beginDateTime").value("2021-03-02T00:00:00"))
                .andExpect(jsonPath("$.endDateTime").value("2021-03-04T12:00:00"))
                .andExpect(jsonPath("$.timeLimit").value("PT1H"))
                .andExpect(jsonPath("$.passScore").value(70))
                .andExpect(jsonPath("$.cancelled").value(false))
                .andExpect(jsonPath("$.finished").value(false));

            // Document
            actions.andDo(document("exam-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_CLASSROOM_ID,
                    DOC_FIELD_CREATOR_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_BEGIN_DATE_TIME,
                    DOC_FIELD_END_DATE_TIME,
                    DOC_FIELD_TIME_LIMIT,
                    DOC_FIELD_PASS_SCORE,
                    DOC_FIELD_CANCELLED,
                    DOC_FIELD_FINISHED
                )));
        }

        @Test
        void getExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/exams/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SearchExams {

        @Test
        void listExams() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam examA = entityHelper.generateExam();
                Exam examB = entityHelper.generateExam();

                return new Struct()
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId());
            });
            Long examAId = given.valueOf("examAId");
            Long examBId = given.valueOf("examBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exams"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examAId.intValue(),
                    examBId.intValue()
                )));

            // Document
            actions.andDo(document("exam-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listExamsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateExam();
                Exam examA = entityHelper.generateExam();
                Exam examB = entityHelper.generateExam();

                return new Struct()
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId());
            });
            Long examAId = given.valueOf("examAId");
            Long examBId = given.valueOf("examBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    examBId.intValue(),
                    examAId.intValue()
                )));
        }

        @Test
        void searchExamsByClassroomId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Exam examA = entityHelper.generateExam(it -> it.withClassroom(classroom));
                Exam examB = entityHelper.generateExam(it -> it.withClassroom(classroom));
                entityHelper.generateExam();

                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long examAId = given.valueOf("examAId");
            Long examBId = given.valueOf("examBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("classroomId", classroomId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examAId.intValue(),
                    examBId.intValue()
                )));

            // Document
            actions.andDo(document("exam-search-example",
                requestParameters(
                    parameterWithName("classroomId")
                        .description("소속된 반 ID").optional(),
                    parameterWithName("creatorId")
                        .description("생성한 선생님 ID").optional(),
                    parameterWithName("studentId")
                        .description("시험 응시생(소속된 반의 수강생) ID").optional(),
                    parameterWithName("finished")
                        .description("종료된 시험").optional(),
                    parameterWithName("cancelled")
                        .description("취소된 시험").optional()
                )));
        }

        @Test
        void searchExamsByCreatorId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher creator = entityHelper.generateTeacher();

                Exam examA = entityHelper.generateExam(it -> it.withCreator(creator));
                Exam examB = entityHelper.generateExam(it -> it.withCreator(creator));
                entityHelper.generateExam();

                return new Struct()
                    .withValue("creatorId", creator.getId())
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId());
            });
            Long creatorId = given.valueOf("creatorId");
            Long examAId = given.valueOf("examAId");
            Long examBId = given.valueOf("examBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("creatorId", creatorId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examAId.intValue(),
                    examBId.intValue()
                )));
        }

        @Test
        void searchExamsByStudentId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent(it -> {
                    classroom.addStudent(it);
                    return it;
                });

                Exam examA = entityHelper.generateExam(it -> it.withClassroom(classroom));
                Exam examB = entityHelper.generateExam(it -> it.withClassroom(classroom));
                entityHelper.generateExam();

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long examAId = given.valueOf("examAId");
            Long examBId = given.valueOf("examBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("studentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examAId.intValue(),
                    examBId.intValue()
                )));
        }

        @Test
        void searchExamsByFinished() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Exam examA = entityHelper.generateExam(it -> it.withClassroom(classroom)
                    .withFinished(false));
                Exam examB = entityHelper.generateExam(it -> it.withClassroom(classroom)
                    .withFinished(true));
                Exam examC = entityHelper.generateExam(it -> it.withClassroom(classroom)
                    .withFinished(true));
                entityHelper.generateExam();

                return new Struct()
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId())
                    .withValue("examCId", examC.getId());
            });
            Long examBId = given.valueOf("examBId");
            Long examCId = given.valueOf("examCId");


            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("finished", String.valueOf(true)));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examBId.intValue(),
                    examCId.intValue()
                )));
        }

        @Test
        void searchExamsByCancelled() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Exam examA = entityHelper.generateExam(it -> it.withClassroom(classroom)
                    .withCancelled(false));
                Exam examB = entityHelper.generateExam(it -> it.withClassroom(classroom)
                    .withCancelled(true));
                Exam examC = entityHelper.generateExam(it -> it.withClassroom(classroom)
                    .withCancelled(true));
                entityHelper.generateExam();

                return new Struct()
                    .withValue("examAId", examA.getId())
                    .withValue("examBId", examB.getId())
                    .withValue("examCId", examC.getId());
            });
            Long examBId = given.valueOf("examBId");
            Long examCId = given.valueOf("examCId");


            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("cancelled", String.valueOf(true)));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examBId.intValue(),
                    examCId.intValue()
                )));
        }
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void postExam() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Classroom classroom = entityHelper.generateClassroom();
            Teacher creator = entityHelper.generateTeacher();

            return new Struct()
                .withValue("classroomId", classroom.getId())
                .withValue("creatorId", creator.getId());
        });
        Long classroomId = given.valueOf("classroomId");
        Long creatorId = given.valueOf("creatorId");

        // When
        ExamDto.Create dto = ExamDto.Create.builder()
            .classroomId(classroomId)
            .creatorId(creatorId)
            .name("3월 모의고사")
            .beginDateTime(LocalDateTime.of(2021, 3, 2, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 3, 4, 12, 0))
            .timeLimit(Duration.ofHours(1))
            .passScore(70)
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/exams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.classroomId").value(classroomId))
            .andExpect(jsonPath("$.creatorId").value(creatorId))
            .andExpect(jsonPath("$.name").value("3월 모의고사"))
            .andExpect(jsonPath("$.beginDateTime").value("2021-03-02T00:00:00"))
            .andExpect(jsonPath("$.endDateTime").value("2021-03-04T12:00:00"))
            .andExpect(jsonPath("$.timeLimit").value("PT1H"))
            .andExpect(jsonPath("$.passScore").value(70))
            .andExpect(jsonPath("$.cancelled").value(false))
            .andExpect(jsonPath("$.finished").value(false));

        // Document
        actions.andDo(document("exam-create-example",
            requestFields(
                DOC_FIELD_CLASSROOM_ID,
                DOC_FIELD_CREATOR_ID,
                DOC_FIELD_NAME,
                DOC_FIELD_BEGIN_DATE_TIME,
                DOC_FIELD_END_DATE_TIME,
                DOC_FIELD_TIME_LIMIT,
                DOC_FIELD_PASS_SCORE.optional()
            )));
    }

    @Nested
    class PatchExam {

        @Test
        @WithMockUser(authorities = "ADMIN")
        void patchExam() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it ->
                    it.withName("3월 모의고사")
                        .withBeginDateTime(LocalDateTime.of(2021, 3, 2, 0, 0))
                        .withEndDateTime(LocalDateTime.of(2021, 3, 4, 12, 0))
                        .withTimeLimit(Duration.ofHours(1))
                        .withPassScore(70)
                );
                return exam.getId();
            });

            // When
            Update dto = Update.builder()
                .name("4월 모의고사")
                .beginDateTime(LocalDateTime.of(2021, 4, 12, 0, 0))
                .endDateTime(LocalDateTime.of(2021, 4, 17, 12, 30))
                .timeLimit(Duration.ofHours(3))
                .passScore(80)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exams/{id}", examId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("4월 모의고사"))
                .andExpect(jsonPath("$.beginDateTime").value("2021-04-12T00:00:00"))
                .andExpect(jsonPath("$.endDateTime").value("2021-04-17T12:30:00"))
                .andExpect(jsonPath("$.timeLimit").value("PT3H"))
                .andExpect(jsonPath("$.passScore").value(80));

            // Document
            actions.andDo(document("exam-update-example",
                requestFields(
                    DOC_FIELD_NAME.optional(),
                    DOC_FIELD_BEGIN_DATE_TIME.optional(),
                    DOC_FIELD_END_DATE_TIME.optional(),
                    DOC_FIELD_TIME_LIMIT.optional(),
                    DOC_FIELD_PASS_SCORE.optional()
                )));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void patchExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anExamUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/exams/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteExam {

        @Test
        @WithMockUser(authorities = "ADMIN")
        void deleteExam() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exams/{id}", examId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(examRepository.findById(examId)).isEmpty();

            // Document
            actions.andDo(document("exam-delete-example"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void deleteExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/exams/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class CancelExam {

        @Test
        @WithMockUser(authorities = "ADMIN")
        void cancelExam() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exams/{id}/cancel", examId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            boolean isCancelled = trxHelper.doInTransaction(() -> {
                Exam exam = examRepository.findById(examId).orElseThrow();
                return exam.isCancelled();
            });

            assertThat(isCancelled).isTrue();

            // Document
            actions.andDo(document("exam-cancel-example"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void cancelExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/cancel", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void cancelExam_ExamAlreadyFinished_BadRequestStatus() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it -> it.withFinished(true));
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/cancel", examId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void cancelExam_ExamAlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it -> it.withCancelled(true));
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/cancel", examId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class FinishExam {


        @Test
        @WithMockUser(authorities = "ADMIN")
        void finishExam() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(post("/exams/{id}/finish", examId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            boolean isFinished = trxHelper.doInTransaction(() -> {
                Exam exam = examRepository.findById(examId).orElseThrow();
                return exam.isFinished();
            });

            assertThat(isFinished).isTrue();

            // Document
            actions.andDo(document("exam-finish-example"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void finishExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/finish", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void finishExam_ExamAlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it -> it.withCancelled(true));
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/finish", examId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void finishExam_ExamAlreadyFinished_BadRequestStatus() throws Exception {
            // Given
            Long examId = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam(it -> it.withFinished(true));
                return exam.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/finish", examId));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

}
