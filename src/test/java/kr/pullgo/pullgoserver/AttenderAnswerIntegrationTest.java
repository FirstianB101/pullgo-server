package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.helper.AuthHelper;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AttenderAnswerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttenderAnswerRepository attenderAnswerRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private AuthHelper authHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Nested
    class GetAttenderAnswer {

        @Test
        void getAttenderAnswer() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderAnswer attenderAnswer = entityHelper.generateAttenderAnswer(it ->
                    it.withAnswer(new Answer(1, 2, 3))
                );

                return new Struct()
                    .withValue("attenderAnswerId", attenderAnswer.getId())
                    .withValue("attenderStateId", attenderAnswer.getAttenderState().getId())
                    .withValue("questionId", attenderAnswer.getQuestion().getId());
            });
            Long attenderAnswerId = given.valueOf("attenderAnswerId");
            Long attenderStateId = given.valueOf("attenderStateId");
            Long questionId = given.valueOf("questionId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/attender-state/{attenderStateId}/answers/{questionId}",
                    attenderStateId, questionId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attenderAnswerId))
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.questionId").value(questionId))
                .andExpect(jsonPath("$.attenderStateId").value(attenderStateId));
        }

        @Test
        void getAttenderAnswer_AttenderAnswerNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(
                get("/exam/attender-state/{attenderStateId}/answers/{questionId}", 0L, 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchAttenderAnswers {

        @Test
        void listAttenderAnswers() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderAnswer attenderAnswerA = entityHelper.generateAttenderAnswer();
                AttenderAnswer attenderAnswerB = entityHelper.generateAttenderAnswer();

                return new Struct()
                    .withValue("attenderAnswerAId", attenderAnswerA.getId())
                    .withValue("attenderAnswerBId", attenderAnswerB.getId());
            });
            Long attenderAnswerAId = given.valueOf("attenderAnswerAId");
            Long attenderAnswerBId = given.valueOf("attenderAnswerBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-state/answers"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderAnswerAId.intValue(),
                    attenderAnswerBId.intValue()
                )));
        }

        @Test
        void listAttenderAnswersWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateAttenderAnswer();
                AttenderAnswer attenderAnswerA = entityHelper.generateAttenderAnswer();
                AttenderAnswer attenderAnswerB = entityHelper.generateAttenderAnswer();

                return new Struct()
                    .withValue("attenderAnswerAId", attenderAnswerA.getId())
                    .withValue("attenderAnswerBId", attenderAnswerB.getId());
            });
            Long attenderAnswerAId = given.valueOf("attenderAnswerAId");
            Long attenderAnswerBId = given.valueOf("attenderAnswerBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-state/answers")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    attenderAnswerBId.intValue(),
                    attenderAnswerAId.intValue()
                )));
        }

        @Test
        void searchAttenderAnswersByAttenderStatesId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState();

                AttenderAnswer attenderAnswerA = entityHelper.generateAttenderAnswer(it ->
                    it.withAttenderState(attenderState)
                );
                AttenderAnswer attenderAnswerB = entityHelper.generateAttenderAnswer(it ->
                    it.withAttenderState(attenderState)
                );

                return new Struct()
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("attenderAnswerAId", attenderAnswerA.getId())
                    .withValue("attenderAnswerBId", attenderAnswerB.getId());
            });
            Long attenderStateId = given.valueOf("attenderStateId");
            Long attenderAnswerAId = given.valueOf("attenderAnswerAId");
            Long attenderAnswerBId = given.valueOf("attenderAnswerBId");

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-state/answers")
                .param("attenderStateId", attenderStateId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderAnswerAId.intValue(),
                    attenderAnswerBId.intValue()
                )));
        }

    }

    @Nested
    class PutAttenderAnswer {

        @Test
        void put으로_AttenderAnswer_신규생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderState attenderState = entityHelper.generateAttenderState();
                Question question = entityHelper.generateQuestion();
                String token = authHelper.generateToken(
                    it -> attenderState.getAttender().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attenderStateId", attenderState.getId())
                    .withValue("questionId", question.getId());
            });
            Long attenderStateId = given.valueOf("attenderStateId");
            Long questionId = given.valueOf("questionId");
            String token = given.valueOf("token");

            // When
            AttenderAnswerDto.Create dto = AttenderAnswerDto.Create.builder()
                .attenderStateId(attenderStateId)
                .questionId(questionId)
                .answer(Set.of(1, 2, 3))
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(
                put("/exam/attender-state/{attenderStateId}/answers/{questionId}", attenderStateId,
                    questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body)).andDo(print());

            // Then
            actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.attenderStateId").value(attenderStateId))
                .andExpect(jsonPath("$.questionId").value(questionId));
        }

        @Test
        void put으로_AttenderAnswer_수정() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderAnswer attenderAnswer = entityHelper.generateAttenderAnswer(it ->
                    it.withAnswer(new Answer(4, 5))
                );
                String token = authHelper.generateToken(
                    it -> attenderAnswer.getAttenderState().getAttender().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attenderStateId", attenderAnswer.getAttenderState().getId())
                    .withValue("questionId", attenderAnswer.getQuestion().getId());
            });
            Long attenderStateId = given.valueOf("attenderStateId");
            Long questionId = given.valueOf("questionId");
            String token = given.valueOf("token");

            // When
            AttenderAnswerDto.Put dto = AttenderAnswerDto.Put.builder()
                .answer(Set.of(1, 2, 3))
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(
                put("/exam/attender-state/{attenderStateId}/answers/{questionId}", attenderStateId,
                    questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.attenderStateId").value(attenderStateId))
                .andExpect(jsonPath("$.questionId").value(questionId));

        }

    }

    @Nested
    class DeleteAttenderAnswer {

        @Test
        void deleteAttenderAnswer() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AttenderAnswer attenderAnswer = entityHelper.generateAttenderAnswer(it ->
                    it.withAnswer(new Answer(4, 5))
                );
                String token = authHelper.generateToken(
                    it -> attenderAnswer.getAttenderState().getAttender().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("attenderAnswerId", attenderAnswer.getId())
                    .withValue("attenderStateId", attenderAnswer.getAttenderState().getId())
                    .withValue("questionId", attenderAnswer.getQuestion().getId());
            });
            Long attenderAnswerId = given.valueOf("attenderAnswerId");
            Long attenderStateId = given.valueOf("attenderStateId");
            Long questionId = given.valueOf("questionId");
            String token = given.valueOf("token");

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/attender-state/{attenderStateId}/answers/{questionId}",
                    attenderStateId, questionId)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(attenderAnswerRepository.findById(attenderAnswerId)).isEmpty();
        }

        @Test
        void deleteAttenderAnswer_AttenderAnswerNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(
                delete("/exam/attender-state/{attenderStateId}/answers/{questionId}", 0L, 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

}
