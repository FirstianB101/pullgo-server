package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
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
import java.util.Set;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.QuestionDto.Update;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
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
    private ClassroomRepository classroomRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(documentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class GetQuestion {

        @Test
        void getQuestion() throws Exception {
            // Given
            Question question = Question.builder()
                .answer(new Answer(1, 2, 3))
                .pictureUrl("Url")
                .content("Contents")
                .build();
            Exam exam = createExam();

            exam.addQuestion(question);
            examRepository.save(exam);

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/questions/{id}", question.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getId()))
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.pictureUrl").value("Url"))
                .andExpect(jsonPath("$.content").value("Contents"))
                .andExpect(jsonPath("$.examId").value(exam.getId()));

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
            Question questionA = createAndSaveQuestion();
            Question questionB = createAndSaveQuestion();

            // When
            ResultActions actions = mockMvc.perform(get("/exam/questions"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    questionA.getId().intValue(),
                    questionB.getId().intValue()
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
            createAndSaveQuestion();
            Question questionA = createAndSaveQuestion();
            Question questionB = createAndSaveQuestion();

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
                    questionB.getId().intValue(),
                    questionA.getId().intValue()
                )));
        }

        @Test
        void searchQuestionsByExamId() throws Exception {
            // Given
            Exam exam = createAndSaveExam();

            Question questionA = createAndSaveQuestionWithExam(exam);
            Question questionB = createAndSaveQuestionWithExam(exam);
            createAndSaveQuestion();

            // When
            ResultActions actions = mockMvc.perform(get("/exam/questions")
                .param("examId", exam.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    questionA.getId().intValue(),
                    questionB.getId().intValue()
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
        Exam exam = createAndSaveExam();

        // When
        QuestionDto.Create dto = QuestionDto.Create.builder()
            .answer(Set.of(1, 2, 3))
            .pictureUrl("Url")
            .content("Contents")
            .examId(exam.getId())
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
            .andExpect(jsonPath("$.examId").value(exam.getId()));

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
            Question question = Question.builder()
                .answer(new Answer(4, 5, 6))
                .pictureUrl("Before url")
                .content("Before contents")
                .build();
            Exam exam = createExam();

            exam.addQuestion(question);
            examRepository.save(exam);

            // When
            Update dto = Update.builder()
                .answer(Set.of(1, 2, 3))
                .pictureUrl("Url")
                .content("Contents")
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exam/questions/{id}", question.getId())
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
                .andExpect(jsonPath("$.examId").value(exam.getId()));

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
            String body = toJson(questionUpdateDto());

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
            Question question = createAndSaveQuestion();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/questions/{id}", question.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(questionRepository.findById(question.getId())).isEmpty();

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

    private Duration stringToDuration(String duration) {
        return Duration.parse(duration);
    }

    private LocalDateTime stringToLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

    private Exam createExam() {
        return Exam.builder()
            .name("test name")
            .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
            .timeLimit(stringToDuration("PT1H"))
            .passScore(70)
            .build();
    }

    private Exam createAndSaveExam() {
        return examRepository.save(Exam.builder()
            .name("test name")
            .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
            .timeLimit(stringToDuration("PT1H"))
            .passScore(70)
            .build());
    }

    private Question createAndSaveQuestion() {
        Question question = Question.builder()
            .answer(new Answer(4, 5, 6))
            .pictureUrl("Before url")
            .content("Before contents")
            .build();
        Exam exam = createExam();
        exam.addQuestion(question);
        examRepository.save(exam);

        return question;
    }

    private Question createAndSaveQuestionWithExam(Exam exam) {
        Question question = Question.builder()
            .answer(new Answer(4, 5, 6))
            .pictureUrl("Before url")
            .content("Before contents")
            .build();
        exam.addQuestion(question);

        return questionRepository.save(question);
    }

    private Update questionUpdateDto() {
        return Update.builder()
            .answer(Set.of(1, 2, 3))
            .pictureUrl("Url")
            .content("Contents")
            .build();
    }
}
