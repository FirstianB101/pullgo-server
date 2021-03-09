package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
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
import java.util.Set;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto.Update;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
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
public class AttenderAnswerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttenderAnswerRepository attenderAnswerRepository;

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Nested
    class GetAttenderAnswer {

        @Test
        void getAttenderAnswer() throws Exception {
            // Given
            AttenderAnswer attenderAnswer = AttenderAnswer.builder()
                .answer(new Answer(1, 2, 3))
                .build();
            AttenderState attenderState = createAttenderState();
            Question question = createAndSaveQuestion();

            attenderAnswer.setQuestion(question);
            attenderState.addAnswer(attenderAnswer);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/attender-state/answers/{id}", attenderAnswer.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attenderAnswer.getId()))
                .andExpect(jsonPath("$.answer.objectiveNumbers.[0]").value(1))
                .andExpect(jsonPath("$.answer.objectiveNumbers.[1]").value(2))
                .andExpect(jsonPath("$.answer.objectiveNumbers.[2]").value(3))
                .andExpect(jsonPath("$.questionId").value(question.getId()))
                .andExpect(jsonPath("$.attenderStateId").value(attenderState.getId()));
        }

        @Test
        void getAttenderAnswer_AttenderAnswerNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-state/answers/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Test
    void postAttenderAnswer() throws Exception {
        // Given
        AttenderState attenderState = createAndSaveAttenderState();
        Question question = createAndSaveQuestion();

        // When
        AttenderAnswerDto.Create dto = AttenderAnswerDto.Create.builder()
            .attenderStateId(attenderState.getId())
            .questionId(question.getId())
            .answer(Set.of(1, 2, 3))
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/exam/attender-state/answers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andDo(print());

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.answer.objectiveNumbers.[0]").value(1))
            .andExpect(jsonPath("$.answer.objectiveNumbers.[1]").value(2))
            .andExpect(jsonPath("$.answer.objectiveNumbers.[2]").value(3))
            .andExpect(jsonPath("$.attenderStateId").value(attenderState.getId()))
            .andExpect(jsonPath("$.questionId").value(question.getId()));
    }

    @Nested
    class PatchAttenderAnswer {

        @Test
        void patchAttenderAnswer() throws Exception {
            // Given
            AttenderAnswer attenderAnswer = AttenderAnswer.builder()
                .answer(new Answer(4, 5, 6))
                .build();
            AttenderState attenderState = createAndSaveAttenderState();
            Question question = createAndSaveQuestion();

            attenderAnswer.setQuestion(question);
            attenderState.addAnswer(attenderAnswer);

            attenderAnswerRepository.save(attenderAnswer);

            // When
            AttenderAnswerDto.Update dto = Update.builder()
                .answer(Set.of(1, 2, 3))
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exam/attender-state/answers/{id}", attenderAnswer.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.answer.objectiveNumbers.[0]").value(1))
                .andExpect(jsonPath("$.answer.objectiveNumbers.[1]").value(2))
                .andExpect(jsonPath("$.answer.objectiveNumbers.[2]").value(3))
                .andExpect(jsonPath("$.attenderStateId").value(attenderState.getId()))
                .andExpect(jsonPath("$.questionId").value(question.getId()));

        }

        @Test
        void patchAttenderAnswer_AttenderAnswerNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(attenderAnswerUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/exam/attender-state/answers/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteAttenderAnswer {

        @Test
        void deleteAttenderAnswer() throws Exception {
            // Given
            AttenderAnswer attenderAnswer = createAndSaveAttenderAnswer();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exam/attender-state/answers/{id}", attenderAnswer.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(attenderAnswerRepository.findById(attenderAnswer.getId())).isEmpty();
        }

        @Test
        void deleteAttenderAnswer_AttenderAnswerNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/exam/attender-state/answers/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private AttenderState createAttenderState() {
        return new AttenderState();
    }

    private AttenderState createAndSaveAttenderState() {
        return attenderStateRepository.save(new AttenderState());
    }

    private AttenderAnswer createAndSaveAttenderAnswer() {
        AttenderAnswer attenderAnswer = AttenderAnswer.builder()
            .answer(new Answer(4, 5, 6))
            .build();
        AttenderState attenderState = createAttenderState();
        attenderState.addAnswer(attenderAnswer);
        attenderStateRepository.save(attenderState);

        return attenderAnswer;
    }

    private Question createAndSaveQuestion() {
        return questionRepository.save(Question.builder()
            .answer(new Answer(4, 5, 6))
            .pictureUrl("Before url")
            .content("Before contents")
            .build());
    }

    private Update attenderAnswerUpdateDto() {
        return Update.builder()
            .answer(new Answer(1, 2, 3).getObjectiveNumbers())
            .build();
    }
}
