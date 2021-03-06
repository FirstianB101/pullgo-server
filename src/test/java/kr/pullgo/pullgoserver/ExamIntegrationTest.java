package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
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
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.dto.ExamDto.Update;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
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
public class ExamIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AttenderStateRepository attenderStateRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
    }

    @Nested
    class GetExam {

        @Test
        void getExam() throws Exception {
            // Given
            Exam exam = Exam.builder()
                .name("test name")
                .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
                .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
                .timeLimit(stringToDuration("PT1H"))
                .passScore(70)
                .build();
            Teacher creator = createAndSaveTeacher();
            Classroom classroom = createAndSaveClassroom();

            exam.setCreator(creator);
            classroom.addExam(exam);

            classroomRepository.save(classroom);
            examRepository.save(exam);

            // When
            ResultActions actions = mockMvc
                .perform(get("/exams/{id}", exam.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exam.getId()))
                .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
                .andExpect(jsonPath("$.creatorId").value(creator.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.beginDateTime").value("2021-03-02T00:00:00"))
                .andExpect(jsonPath("$.endDateTime").value("2021-03-04T12:00:00"))
                .andExpect(jsonPath("$.timeLimit").value("PT1H"))
                .andExpect(jsonPath("$.passScore").value(70))
                .andExpect(jsonPath("$.cancelled").value(false))
                .andExpect(jsonPath("$.finished").value(false));
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
            Exam examA = createAndSaveExam();
            Exam examB = createAndSaveExam();

            // When
            ResultActions actions = mockMvc.perform(get("/exams"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examA.getId().intValue(),
                    examB.getId().intValue()
                )));
        }

        @Test
        void searchExamsByClassroomId() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            Exam examA = createAndSaveExamWithClassroom(classroom);
            Exam examB = createAndSaveExamWithClassroom(classroom);
            createAndSaveExam();

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("classroomId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examA.getId().intValue(),
                    examB.getId().intValue()
                )));
        }

        @Test
        void searchExamsByCreatorId() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Teacher creator = createAndSaveTeacher();

            Exam examA = createAndSaveExamWithClassroomAndCreator(classroom, creator);
            Exam examB = createAndSaveExamWithClassroomAndCreator(classroom, creator);
            createAndSaveExam();

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("creatorId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examA.getId().intValue(),
                    examB.getId().intValue()
                )));
        }

        @Test
        void searchExamsByStudentId() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Student student = createAndSaveStudent();

            student.applyClassroom(classroom);
            classroom.acceptStudent(student);
            classroomRepository.save(classroom);

            Exam examA = createAndSaveExamWithClassroom(classroom);
            Exam examB = createAndSaveExamWithClassroom(classroom);
            createAndSaveExam();

            // When
            ResultActions actions = mockMvc.perform(get("/exams")
                .param("studentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    examA.getId().intValue(),
                    examB.getId().intValue()
                )));
        }

    }

    @Test
    void postExam() throws Exception {
        // Given
        Teacher creator = createAndSaveTeacher();
        Classroom classroom = createAndSaveClassroom();

        // When
        ExamDto.Create dto = ExamDto.Create.builder()
            .classroomId(classroom.getId())
            .creatorId(creator.getId())
            .name("test name")
            .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
            .timeLimit(stringToDuration("PT1H"))
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
            .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$.creatorId").value(creator.getId()))
            .andExpect(jsonPath("$.name").value("test name"))
            .andExpect(jsonPath("$.beginDateTime").value("2021-03-02T00:00:00"))
            .andExpect(jsonPath("$.endDateTime").value("2021-03-04T12:00:00"))
            .andExpect(jsonPath("$.timeLimit").value("PT1H"))
            .andExpect(jsonPath("$.passScore").value(70))
            .andExpect(jsonPath("$.cancelled").value(false))
            .andExpect(jsonPath("$.finished").value(false));
    }

    @Nested
    class PatchExam {

        @Test
        void patchExam() throws Exception {
            // Given
            Exam exam = Exam.builder()
                .name("before name")
                .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
                .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
                .timeLimit(stringToDuration("PT1H"))
                .passScore(70)
                .build();
            Teacher creator = createAndSaveTeacher();
            Classroom classroom = createAndSaveClassroom();

            exam.setCreator(creator);
            classroom.addExam(exam);

            classroomRepository.save(classroom);
            examRepository.save(exam);

            // When
            Update dto = Update.builder()
                .name("test name")
                .beginDateTime(stringToLocalDateTime("2021-05-12T00:00:00"))
                .endDateTime(stringToLocalDateTime("2021-05-17T12:30:00"))
                .timeLimit(stringToDuration("PT3H"))
                .passScore(80)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/exams/{id}", exam.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.beginDateTime").value("2021-05-12T00:00:00"))
                .andExpect(jsonPath("$.endDateTime").value("2021-05-17T12:30:00"))
                .andExpect(jsonPath("$.timeLimit").value("PT3H"))
                .andExpect(jsonPath("$.passScore").value(80));
        }

        @Test
        void patchExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(examUpdateDto());

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
        void deleteExam() throws Exception {
            // Given
            Exam exam = createAndSaveExam();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/exams/{id}", exam.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(examRepository.findById(exam.getId())).isEmpty();
        }

        @Test
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
        void cancelExam() throws Exception {
            // Given
            Exam exam = createAndSaveExam();

            // When
            ResultActions actions = mockMvc
                .perform(post("/exams/{id}/cancel", exam.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            Exam result = findExamById(exam.getId());

            assertThat(result.isCancelled()).isTrue();
        }

        @Test
        void cancelExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/cancel", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void cancelExam_ExamAlreadyFinished_BadRequestStatus() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            exam.setFinished(true);
            examRepository.save(exam);

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/cancel", exam.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void cancelExam_ExamAlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            exam.setCancelled(true);
            examRepository.save(exam);

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/cancel", exam.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class FinishExam {

        @Test
        void finishExam() throws Exception {
            // Given
            Exam exam = createAndSaveExam();

            // When
            ResultActions actions = mockMvc
                .perform(post("/exams/{id}/finish", exam.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            Exam result = findExamById(exam.getId());

            assertThat(result.isFinished()).isTrue();
        }

        @Test
        void finishExam_ExamNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/finish", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void finishExam_ExamAlreadyCancelled_BadRequestStatus() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            exam.setCancelled(true);
            examRepository.save(exam);

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/finish", exam.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void finishExam_ExamAlreadyFinished_BadRequestStatus() throws Exception {
            // Given
            Exam exam = createAndSaveExam();
            exam.setFinished(true);
            examRepository.save(exam);

            // When
            ResultActions actions = mockMvc.perform(post("/exams/{id}/finish", exam.getId()));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    private Exam findExamById(Long id) {
        return examRepository.findById(id).orElseThrow();
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private Teacher createAndSaveTeacher() {
        Account account = accountRepository.save(
            Account.builder()
                .username("JottsungE")
                .fullName("Kim eun seong")
                .password("mincho")
                .build()
        );
        Teacher teacher = teacherRepository.save(
            new Teacher()
        );
        teacher.setAccount(account);
        return teacher;
    }

    private Duration stringToDuration(String duration) {
        return Duration.parse(duration);
    }

    private LocalDateTime stringToLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

    private Classroom createAndSaveClassroom() {
        return classroomRepository.save(
            Classroom.builder()
                .name("test classroom")
                .build());
    }

    private AttenderState createAndSaveAttenderState() {
        AttenderState attenderState = new AttenderState();
        attenderState.setAttender(createAndSaveStudent());
        attenderState.setProgress(AttendingProgress.COMPLETE);
        attenderState.setScore(100);
        return attenderStateRepository.save(attenderState);
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

    private Question createQuestion() {
        return Question.builder()
            .answer(new Answer(1, 2, 3))
            .pictureUrl("Url")
            .content("Contents")
            .build();
    }

    private Exam createAndSaveExam() {
        Exam exam = Exam.builder()
            .name("test name")
            .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
            .timeLimit(stringToDuration("PT1H"))
            .passScore(70)
            .build();
        Teacher creator = createAndSaveTeacher();
        AttenderState attenderState = createAndSaveAttenderState();
        Question question = createQuestion();
        Classroom classroom = createAndSaveClassroom();

        exam.setCreator(creator);
        attenderState.setExam(exam);
        exam.addQuestion(question);
        classroom.addExam(exam);

        classroomRepository.save(classroom);
        return examRepository.save(exam);
    }

    private Exam createAndSaveExamWithClassroom(Classroom classroom) {
        Exam exam = Exam.builder()
            .name("test name")
            .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
            .timeLimit(stringToDuration("PT1H"))
            .passScore(70)
            .build();
        Teacher creator = createAndSaveTeacher();
        AttenderState attenderState = createAndSaveAttenderState();
        Question question = createQuestion();

        exam.setCreator(creator);
        attenderState.setExam(exam);
        exam.addQuestion(question);
        classroom.addExam(exam);

        return examRepository.save(exam);
    }

    private Exam createAndSaveExamWithClassroomAndCreator(Classroom classroom, Teacher creator) {
        Exam exam = Exam.builder()
            .name("test name")
            .beginDateTime(stringToLocalDateTime("2021-03-02T00:00:00"))
            .endDateTime(stringToLocalDateTime("2021-03-04T12:00:00"))
            .timeLimit(stringToDuration("PT1H"))
            .passScore(70)
            .build();
        AttenderState attenderState = createAndSaveAttenderState();
        Question question = createQuestion();

        exam.setCreator(creator);
        attenderState.setExam(exam);
        exam.addQuestion(question);
        classroom.addExam(exam);

        return examRepository.save(exam);
    }

    private Update examUpdateDto() {
        return Update.builder()
            .name("test name")
            .build();
    }
}
