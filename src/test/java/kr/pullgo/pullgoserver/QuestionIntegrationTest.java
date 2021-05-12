package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.QuestionHelper.aQuestionUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.QuestionDto.Update;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
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
public class QuestionIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("문제 ID");
    private static final FieldDescriptor DOC_FIELD_ANSWER =
        fieldWithPath("answer").description("정답 (객관식, 1~5 범위의 정수 배열)");
    private static final FieldDescriptor DOC_FIELD_PICTURE_URL =
        fieldWithPath("pictureUrl").description("첨부된 사진의 URL");
    private static final FieldDescriptor DOC_FIELD_CONTENT =
        fieldWithPath("content").description("문제 내용");
    private static final FieldDescriptor DOC_FIELD_EXAM_ID =
        fieldWithPath("examId").description("소속된 시험 ID");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class GetQuestion {

        @Test
        void getQuestion() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Question question = entityHelper.generateQuestion(it ->
                    it.withContent("Contents")
                        .withPictureUrl("Url")
                        .withAnswer(new Answer(1, 2, 3))
                );
                return new Struct()
                    .withValue("questionId", question.getId())
                    .withValue("examId", question.getExam().getId());
            });
            Long questionId = given.valueOf("questionId");
            Long examId = given.valueOf("examId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/questions/{id}", questionId)).andDo(print());

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questionId))
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.pictureUrl").value("Url"))
                .andExpect(jsonPath("$.content").value("Contents"))
                .andExpect(jsonPath("$.examId").value(examId));

            // Document
            actions.andDo(document("question-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_ANSWER,
                    DOC_FIELD_PICTURE_URL,
                    DOC_FIELD_CONTENT,
                    DOC_FIELD_EXAM_ID
                )));
        }

        @Test
        void getQuestion_QuestionNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/exam/questions/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchQuestions {

        @Test
        void listQuestions() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Question questionA = entityHelper.generateQuestion();
                Question questionB = entityHelper.generateQuestion();
                return new Struct()
                    .withValue("questionAId", questionA.getId())
                    .withValue("questionBId", questionB.getId());
            });
            Long questionAId = given.valueOf("questionAId");
            Long questionBId = given.valueOf("questionBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/questions"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    questionAId.intValue(),
                    questionBId.intValue()
                )));

            // Document
            actions.andDo(document("question-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listQuestionsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateQuestion();
                Question questionA = entityHelper.generateQuestion();
                Question questionB = entityHelper.generateQuestion();
                return new Struct()
                    .withValue("questionAId", questionA.getId())
                    .withValue("questionBId", questionB.getId());
            });
            Long questionAId = given.valueOf("questionAId");
            Long questionBId = given.valueOf("questionBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/questions")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    questionBId.intValue(),
                    questionAId.intValue()
                )));
        }

        @Test
        void searchQuestionsByExamId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();

                Question questionA = entityHelper.generateQuestion(it -> it.withExam(exam));
                Question questionB = entityHelper.generateQuestion(it -> it.withExam(exam));
                return new Struct()
                    .withValue("examId", exam.getId())
                    .withValue("questionAId", questionA.getId())
                    .withValue("questionBId", questionB.getId());
            });
            Long examId = given.valueOf("examId");
            Long questionAId = given.valueOf("questionAId");
            Long questionBId = given.valueOf("questionBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/questions")
                .param("examId", examId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    questionAId.intValue(),
                    questionBId.intValue()
                )));

            // Document
            actions.andDo(document("question-search-example",
                requestParameters(
                    parameterWithName("examId").description("소속된 시험 ID").optional()
                )));
        }

    }

    @Test
    void postQuestion() throws Exception {
        // Given
        Long examId = trxHelper.doInTransaction(() -> {
            Exam exam = entityHelper.generateExam();
            return exam.getId();
        });

        // When
        QuestionDto.Create dto = QuestionDto.Create.builder()
            .answer(Set.of(1, 2, 3))
            .pictureUrl("Url")
            .content("Contents")
            .examId(examId)
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/exam/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.answer.[0]").value(1))
            .andExpect(jsonPath("$.answer.[1]").value(2))
            .andExpect(jsonPath("$.answer.[2]").value(3))
            .andExpect(jsonPath("$.pictureUrl").value("Url"))
            .andExpect(jsonPath("$.content").value("Contents"))
            .andExpect(jsonPath("$.examId").value(examId));

        // Document
        actions.andDo(document("question-create-example",
            requestFields(
                DOC_FIELD_ANSWER,
                DOC_FIELD_PICTURE_URL.optional(),
                DOC_FIELD_CONTENT,
                DOC_FIELD_EXAM_ID
            )));
    }

    @Nested
    class PatchQuestion {

        @Test
        void patchQuestion() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Question question = entityHelper.generateQuestion(it ->
                    it.withAnswer(new Answer(4, 5, 6))
                        .withContent("Before contents")
                        .withPictureUrl("Before url")
                );
                return new Struct()
                    .withValue("questionId", question.getId())
                    .withValue("examId", question.getExam().getId());
            });
            Long questionId = given.valueOf("questionId");
            Long examId = given.valueOf("examId");

            // When
            Update dto = Update.builder()
                .answer(Set.of(1, 2, 3))
                .pictureUrl("Url")
                .content("Contents")
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exam/questions/{id}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.pictureUrl").value("Url"))
                .andExpect(jsonPath("$.content").value("Contents"))
                .andExpect(jsonPath("$.examId").value(examId));

            // Document
            actions.andDo(document("question-update-example",
                requestFields(
                    DOC_FIELD_ANSWER.optional(),
                    DOC_FIELD_PICTURE_URL.optional(),
                    DOC_FIELD_CONTENT.optional()
                )));
        }

        @Test
        void patchQuestion_QuestionNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aQuestionUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/exam/questions/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteQuestion {

        @Test
        void deleteQuestion() throws Exception {
            // Given
            Long questionId = trxHelper.doInTransaction(() -> {
                Question question = entityHelper.generateQuestion();
                return question.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/questions/{id}", questionId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(questionRepository.findById(questionId)).isEmpty();

            // Document
            actions.andDo(document("question-delete-example"));
        }

        @Test
        void deleteQuestion_QuestionNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/exam/questions/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

}
