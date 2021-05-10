package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademy;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static kr.pullgo.pullgoserver.helper.AttenderAnswerHelper.anAttenderAnswerUpdateDto;
import static kr.pullgo.pullgoserver.helper.AttenderStateHelper.anAttenderState;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudent;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
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
import java.time.LocalDateTime;
import java.util.Set;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto.Update;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
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
    private AcademyRepository academyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
    }

    @Nested
    class GetAttenderAnswer {

        @Test
        void getAttenderAnswer() throws Exception {
            // Given
            AttenderAnswer attenderAnswer = AttenderAnswer.builder()
                .answer(new Answer(1, 2, 3))
                .build();
            AttenderState attenderState = anAttenderState().withId(null);
            Question question = createAndSaveQuestion();

            attenderAnswer.setQuestion(question);
            attenderState.setExam(createAndSaveExam());
            attenderState.setAttender(createAndSaveStudent());
            attenderState.addAnswer(attenderAnswer);
            attenderStateRepository.save(attenderState);

            // When
            ResultActions actions = mockMvc
                .perform(get("/exam/attender-state/answers/{id}", attenderAnswer.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attenderAnswer.getId()))
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
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

    @Nested
    class SearchAttenderAnswers {

        @Test
        void listAttenderAnswers() throws Exception {
            // Given
            AttenderAnswer attenderAnswerA = createAndSaveAttenderAnswer();
            AttenderAnswer attenderAnswerB = createAndSaveAttenderAnswer();

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-state/answers"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderAnswerA.getId().intValue(),
                    attenderAnswerB.getId().intValue()
                )));
        }

        @Test
        void listAttenderAnswersWithPaging() throws Exception {
            // Given
            createAndSaveAttenderAnswer();
            AttenderAnswer attenderAnswerA = createAndSaveAttenderAnswer();
            AttenderAnswer attenderAnswerB = createAndSaveAttenderAnswer();

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
                    attenderAnswerB.getId().intValue(),
                    attenderAnswerA.getId().intValue()
                )));
        }

        @Test
        void searchAttenderAnswersByAttenderStatesId() throws Exception {
            // Given
            AttenderState attenderState = createAndSaveAttenderState();

            AttenderAnswer attenderAnswerA = createAndSaveAttenderAnswerWithAttenderState(
                attenderState);
            AttenderAnswer attenderAnswerB = createAndSaveAttenderAnswerWithAttenderState(
                attenderState);

            // When
            ResultActions actions = mockMvc.perform(get("/exam/attender-state/answers")
                .param("attenderStateId", attenderState.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    attenderAnswerA.getId().intValue(),
                    attenderAnswerB.getId().intValue()
                )));
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
            .andExpect(jsonPath("$.answer.[0]").value(1))
            .andExpect(jsonPath("$.answer.[1]").value(2))
            .andExpect(jsonPath("$.answer.[2]").value(3))
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
                .andExpect(jsonPath("$.answer.[0]").value(1))
                .andExpect(jsonPath("$.answer.[1]").value(2))
                .andExpect(jsonPath("$.answer.[2]").value(3))
                .andExpect(jsonPath("$.attenderStateId").value(attenderState.getId()))
                .andExpect(jsonPath("$.questionId").value(question.getId()));

        }

        @Test
        void patchAttenderAnswer_AttenderAnswerNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAttenderAnswerUpdateDto());

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

    private AttenderState createAndSaveAttenderState() {
        return attenderStateRepository
            .save(AttenderState.builder().examStartTime(LocalDateTime.now()).build());
    }

    private AttenderAnswer createAndSaveAttenderAnswer() {
        AttenderAnswer attenderAnswer = AttenderAnswer.builder()
            .answer(new Answer(4, 5, 6))
            .build();

        AttenderState attenderState = createAndSaveAttenderState();
        attenderState.addAnswer(attenderAnswer);

        Question question = createAndSaveQuestion();
        attenderAnswer.setQuestion(question);

        return attenderAnswerRepository.save(attenderAnswer);
    }

    private AttenderAnswer createAndSaveAttenderAnswerWithAttenderState(
        AttenderState attenderState) {
        AttenderAnswer attenderAnswer = AttenderAnswer.builder()
            .answer(new Answer(4, 5, 6))
            .build();
        attenderState.addAnswer(attenderAnswer);

        Question question = createAndSaveQuestion();
        attenderAnswer.setQuestion(question);

        return attenderAnswerRepository.save(attenderAnswer);
    }

    private Question createAndSaveQuestion() {
        return questionRepository.save(Question.builder()
            .answer(new Answer(4, 5, 6))
            .pictureUrl("Before url")
            .content("Before contents")
            .build());
    }

    private Student createAndSaveStudent() {
        return studentRepository.save(aStudent()
            .withId(null)
            .withAccount(anAccount().withId(null)));
    }

    private Teacher createAndSaveTeacher() {
        return teacherRepository.save(aTeacher()
            .withId(null)
            .withAccount(anAccount().withId(null)));
    }

    private Academy createAndSaveAcademy() {
        Teacher owner = createAndSaveTeacher();
        Academy academy = anAcademy().withId(null)
            .withTeachers(Set.of(owner))
            .withOwner(owner);

        return academyRepository.save(academy);
    }

    private Classroom createAndSaveClassroom() {
        Classroom classroom = aClassroom().withId(null);

        Academy academy = createAndSaveAcademy();
        classroom.setAcademy(academy);

        return classroomRepository.save(classroom);
    }

    private Exam createAndSaveExam() {
        Exam exam = anExam()
            .withId(null)
            .withClassroom(createAndSaveClassroom())
            .withCreator(createAndSaveTeacher());
        return examRepository.save(exam);
    }

}
