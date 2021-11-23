package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.QuestionHelper.aQuestionCreateDto;
import static kr.pullgo.pullgoserver.helper.QuestionHelper.aQuestionUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.QuestionDto.Create;
import kr.pullgo.pullgoserver.dto.QuestionDto.Update;
import kr.pullgo.pullgoserver.helper.AuthHelper;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.MultipleChoice;
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
    private static final FieldDescriptor DOC_FIELD_MULTIPLE_CHOICE =
        subsectionWithPath("choice").type("MultipleChoice").description("객관식 보기 (1~5 범위의 정수, 보기 내용)");
    private static final FieldDescriptor DOC_FIELD_PICTURE_URL =
        fieldWithPath("pictureUrl").description("첨부된 사진의 URL");
    private static final FieldDescriptor DOC_FIELD_CONTENT =
        fieldWithPath("content").description("문제 내용");
    private static final FieldDescriptor DOC_FIELD_EXAM_ID =
        fieldWithPath("examId").description("소속된 시험 ID");
    private static final FieldDescriptor DOC_FIELD_QUESTIONS =
        subsectionWithPath(".[*]").type("Question").description("Question 생성 필요필드와 동일");

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
    class PostQuestion{

        @Test
        void postQuestion() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                String token = authHelper.generateToken(it -> exam.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("examId", exam.getId());
            });
            String token = given.valueOf("token");
            Long examId = given.valueOf("examId");

            // When
            QuestionDto.Create dto = QuestionDto.Create.builder()
                .content("4보다 작은 자연수는?")
                .pictureUrl("https://i.imgur.com/JOKsNeT.jpg")
                .answer(Set.of(1, 2, 3))
                .choice(Map.of(
                    "1", "1", "2", "2", "3", "3", "4", "4", "5", "5"))
                .examId(examId)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(post("/exam/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("4보다 작은 자연수는?"))
                .andExpect(jsonPath("$.pictureUrl").value("https://i.imgur.com/JOKsNeT.jpg"))
                .andExpect(jsonPath("$.answer").value(containsInAnyOrder(1, 2, 3)))
                .andExpect(jsonPath("$.choice").value(hasEntry("1", "1")))
                .andExpect(jsonPath("$.choice").value(hasEntry("2", "2")))
                .andExpect(jsonPath("$.choice").value(hasEntry("3", "3")))
                .andExpect(jsonPath("$.choice").value(hasEntry("4", "4")))
                .andExpect(jsonPath("$.choice").value(hasEntry("5", "5")))
                .andExpect(jsonPath("$.examId").value(examId));

            // Document
            actions.andDo(document("question-create-example",
                requestFields(
                    DOC_FIELD_ANSWER,
                    DOC_FIELD_MULTIPLE_CHOICE,
                    DOC_FIELD_PICTURE_URL.optional(),
                    DOC_FIELD_CONTENT,
                    DOC_FIELD_EXAM_ID
                )));
        }

        @Test
        void postQuestions() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                String token = authHelper.generateToken(it -> exam.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("examId", exam.getId());
            });
            String token = given.valueOf("token");
            Long examId = given.valueOf("examId");

            // When
            List<Create> dtos = List.of(
                aQuestionCreateDto().withExamId(examId), aQuestionCreateDto().withExamId(examId),
                aQuestionCreateDto().withExamId(examId), aQuestionCreateDto().withExamId(examId));

            String body = toJson(dtos);

            ResultActions actions = mockMvc.perform(post("/exam/questions/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isCreated());

            assertThat(questionRepository.findAll().size()).isEqualTo(4);

            // Document
            actions.andDo(document("questions-create-example",
                requestFields(
                    DOC_FIELD_QUESTIONS
                )));
        }

        @Test
        void 일부_존재하지_않는_Exam_으로_보내는_다중생성_실패() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                String token = authHelper.generateToken(it -> exam.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("examId", exam.getId());
            });
            String token = given.valueOf("token");
            Long examId = given.valueOf("examId");

            // When
            List<Create> dtos = List.of(
                aQuestionCreateDto().withExamId(examId), aQuestionCreateDto().withExamId(5L),
                aQuestionCreateDto().withExamId(examId), aQuestionCreateDto().withExamId(examId));

            String body = toJson(dtos);

            ResultActions actions = mockMvc.perform(post("/exam/questions/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());

            assertThat(questionRepository.findAll()).isEmpty();
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

    @Nested
    class GetQuestion {

        @Test
        void getQuestion() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Question question = entityHelper.generateQuestion(it ->
                    it.withContent("4보다 작은 자연수는?")
                        .withPictureUrl("https://i.imgur.com/JOKsNeT.jpg")
                        .withAnswer(new Answer(1, 2, 3))
                        .withMultipleChoice(new MultipleChoice("1", "2", "3", "4", "5"))
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
                .andExpect(jsonPath("$.content").value("4보다 작은 자연수는?"))
                .andExpect(jsonPath("$.pictureUrl").value("https://i.imgur.com/JOKsNeT.jpg"))
                .andExpect(jsonPath("$.answer").value(containsInAnyOrder(1, 2, 3)))
                .andExpect(jsonPath("$.choice").value(hasEntry("1", "1")))
                .andExpect(jsonPath("$.choice").value(hasEntry("2", "2")))
                .andExpect(jsonPath("$.choice").value(hasEntry("3", "3")))
                .andExpect(jsonPath("$.choice").value(hasEntry("4", "4")))
                .andExpect(jsonPath("$.choice").value(hasEntry("5", "5")))
                .andExpect(jsonPath("$.examId").value(examId));

            // Document
            actions.andDo(document("question-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_ANSWER,
                    DOC_FIELD_MULTIPLE_CHOICE,
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
    class PatchQuestion {

        @Test
        void patchQuestion() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Question question = entityHelper.generateQuestion(it ->
                    it.withContent("2보다 작은 자연수는?")
                        .withPictureUrl("https://i.imgur.com/oPR4BiX.jpeg")
                        .withMultipleChoice(new MultipleChoice("1", "0", "-1", "-2", "-3"))
                        .withAnswer(new Answer(1))
                );
                String token = authHelper.generateToken(it -> question.getExam().getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("questionId", question.getId())
                    .withValue("examId", question.getExam().getId());
            });
            String token = given.valueOf("token");
            Long questionId = given.valueOf("questionId");
            Long examId = given.valueOf("examId");

            // When
            Update dto = Update.builder()
                .content("4보다 작은 자연수는?")
                .pictureUrl("https://i.imgur.com/JOKsNeT.jpg")
                .answer(Set.of(1, 2, 3))
                .choice(Map.of(
                    "1", "1", "2", "2", "3", "3", "4", "4", "5", "5"))
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exam/questions/{id}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("4보다 작은 자연수는?"))
                .andExpect(jsonPath("$.pictureUrl").value("https://i.imgur.com/JOKsNeT.jpg"))
                .andExpect(jsonPath("$.answer").value(containsInAnyOrder(1, 2, 3)))
                .andExpect(jsonPath("$.choice").value(hasEntry("1", "1")))
                .andExpect(jsonPath("$.choice").value(hasEntry("2", "2")))
                .andExpect(jsonPath("$.choice").value(hasEntry("3", "3")))
                .andExpect(jsonPath("$.choice").value(hasEntry("4", "4")))
                .andExpect(jsonPath("$.choice").value(hasEntry("5", "5")))
                .andExpect(jsonPath("$.examId").value(examId));

            // Document
            actions.andDo(document("question-update-example",
                requestFields(
                    DOC_FIELD_ANSWER.optional(),
                    DOC_FIELD_MULTIPLE_CHOICE.optional(),
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

        @Test
        void patchQuestions() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                Question question1 = entityHelper.generateQuestion(it -> it.withExam(exam));
                Question question2 = entityHelper.generateQuestion(it -> it.withExam(exam));
                Question question3 = entityHelper.generateQuestion(it -> it.withExam(exam));
                Question question4 = entityHelper.generateQuestion(it -> it.withExam(exam));

                String token = authHelper.generateToken(it -> exam.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("question1Id", question1.getId())
                    .withValue("question2Id", question2.getId())
                    .withValue("question3Id", question3.getId())
                    .withValue("question4Id", question4.getId());
            });
            String token = given.valueOf("token");
            Long question1Id = given.valueOf("question1Id");
            Long question2Id = given.valueOf("question2Id");
            Long question3Id = given.valueOf("question3Id");
            Long question4Id = given.valueOf("question4Id");

            // When
            Map<Long, QuestionDto.Update> dtos = Map.of(
                question1Id, aQuestionUpdateDto(), question2Id, aQuestionUpdateDto(),
                question3Id, aQuestionUpdateDto(), question4Id, aQuestionUpdateDto());

            String body = toJson(dtos);

            ResultActions actions = mockMvc.perform(patch("/exam/questions/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isOk());
        }

        @Test
        void 일부_존재하지_않는_Question_으로_보내는_다중수정_실패() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Exam exam = entityHelper.generateExam();
                Question question1 = entityHelper.generateQuestion(it -> it.withExam(exam));
                Question question2 = entityHelper.generateQuestion(it -> it.withExam(exam));
                Question question3 = entityHelper.generateQuestion(it -> it.withExam(exam));

                String token = authHelper.generateToken(it -> exam.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("question1Id", question1.getId())
                    .withValue("question2Id", question2.getId())
                    .withValue("question3Id", question3.getId());
            });
            String token = given.valueOf("token");
            Long question1Id = given.valueOf("question1Id");
            Long question2Id = given.valueOf("question2Id");
            Long question3Id = given.valueOf("question3Id");

            // When
            Map<Long, QuestionDto.Update> dtos = Map.of(
                question1Id, aQuestionUpdateDto(), question2Id, aQuestionUpdateDto(),
                question3Id, aQuestionUpdateDto(), 5L, aQuestionUpdateDto());

            String body = toJson(dtos);

            ResultActions actions = mockMvc.perform(patch("/exam/questions/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
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
            Struct given = trxHelper.doInTransaction(() -> {
                Question question = entityHelper.generateQuestion(it ->
                    it.withContent("2보다 작은 자연수는?")
                        .withPictureUrl("https://i.imgur.com/oPR4BiX.jpeg")
                        .withMultipleChoice(new MultipleChoice("1", "0", "-1", "-2", "-3"))
                        .withAnswer(new Answer(1))
                );
                String token = authHelper.generateToken(it -> question.getExam().getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("questionId", question.getId());
            });
            String token = given.valueOf("token");
            Long questionId = given.valueOf("questionId");

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/questions/{id}", questionId)
                    .header("Authorization", "Bearer " + token));

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
