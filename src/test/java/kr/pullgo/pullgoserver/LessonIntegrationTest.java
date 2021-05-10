package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.LessonHelper.aLessonUpdateDto;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aSchedule;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.LessonDto.Update;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
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
public class LessonIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("수업 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("수업 이름");
    private static final FieldDescriptor DOC_FIELD_CLASSROOM_ID =
        fieldWithPath("classroomId").description("소속된 반 ID");
    private static final FieldDescriptor DOC_FIELD_SCHEDULE_DATE =
        fieldWithPath("schedule.date").description("수업 날짜");
    private static final FieldDescriptor DOC_FIELD_SCHEDULE_BEGIN_TIME =
        fieldWithPath("schedule.beginTime").description("수업 시작 시간");
    private static final FieldDescriptor DOC_FIELD_SCHEDULE_END_TIME =
        fieldWithPath("schedule.endTime").description("수업 종료 시간");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class GetLesson {

        @Test
        void getLesson() throws Exception {
            // Given
            Lesson lesson = Lesson.builder()
                .name("test name")
                .build();
            Schedule schedule = Schedule.builder()
                .date(stringToLocalDate("1111-11-11"))
                .beginTime(stringToLocalTime("22:22:22"))
                .endTime(stringToLocalTime("00:00:00"))
                .build();
            Classroom classroom = aClassroom().withId(null).withAcademy(null);

            lesson.setSchedule(schedule);
            classroom.addLesson(lesson);

            classroomRepository.save(classroom);

            // When
            ResultActions actions = mockMvc
                .perform(get("/academy/classroom/lessons/{id}", lesson.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lesson.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
                .andExpect(jsonPath("$.schedule.id").doesNotExist())
                .andExpect(jsonPath("$.schedule.date").value("1111-11-11"))
                .andExpect(jsonPath("$.schedule.beginTime").value("22:22:22"))
                .andExpect(jsonPath("$.schedule.endTime").value("00:00:00"));

            // Document
            actions.andDo(document("lesson-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_CLASSROOM_ID,
                    DOC_FIELD_SCHEDULE_DATE,
                    DOC_FIELD_SCHEDULE_BEGIN_TIME,
                    DOC_FIELD_SCHEDULE_END_TIME
                )));
        }

        @Test
        void getLesson_LessonNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchLessons {

        @Test
        void listLessons() throws Exception {
            // Given
            Lesson lessonA = createAndSaveLesson();
            Lesson lessonB = createAndSaveLesson();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonA.getId().intValue(),
                    lessonB.getId().intValue()
                )));

            // Document
            actions.andDo(document("lesson-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listLessonsWithPaging() throws Exception {
            // Given
            createAndSaveLesson();
            Lesson lessonA = createAndSaveLesson();
            Lesson lessonB = createAndSaveLesson();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    lessonB.getId().intValue(),
                    lessonA.getId().intValue()
                )));
        }

        @Test
        void searchLessonsByClassroomId() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            Lesson lessonA = createAndSaveLessonWithClassroom(classroom);
            Lesson lessonB = createAndSaveLessonWithClassroom(classroom);
            createAndSaveLesson();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("classroomId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonA.getId().intValue(),
                    lessonB.getId().intValue()
                )));

            // Document
            actions.andDo(document("lesson-search-example",
                requestParameters(
                    parameterWithName("classroomId")
                        .description("소속된 반 ID").optional(),
                    parameterWithName("studentId")
                        .description("수업이 배정된 학생(소속된 반의 수강생) ID").optional(),
                    parameterWithName("teacherId")
                        .description("수업이 배정된 선생님(소속된 반의 선생님) ID").optional(),
                    parameterWithName("sinceDate")
                        .description("수업 날짜 시작 범위").optional(),
                    parameterWithName("untilDate")
                        .description("수업 날짜 끝 범위 (exclusive)").optional()
                )));
        }

        @Test
        void searchLessonsByAcademyId() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();
            Classroom classroom = createAndSaveClassroomWithAcademy(academy);

            Lesson lessonA = createAndSaveLessonWithClassroom(classroom);
            Lesson lessonB = createAndSaveLessonWithClassroom(classroom);
            createAndSaveLesson();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("academyId", academy.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonA.getId().intValue(),
                    lessonB.getId().intValue()
                )));
        }

        @Test
        void searchLessonsByStudentId() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            Student student = createAndSaveStudent();
            student.applyClassroom(classroom);
            classroom.acceptStudent(student);

            classroomRepository.save(classroom);

            Lesson lessonA = createAndSaveLessonWithClassroom(classroom);
            Lesson lessonB = createAndSaveLessonWithClassroom(classroom);
            createAndSaveLesson();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("studentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonA.getId().intValue(),
                    lessonB.getId().intValue()
                )));
        }

        @Test
        void searchLessonsByTeacherId() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            Teacher teacher = createAndSaveTeacher();
            teacher.applyClassroom(classroom);
            classroom.acceptTeacher(teacher);

            classroomRepository.save(classroom);

            Lesson lessonA = createAndSaveLessonWithClassroom(classroom);
            Lesson lessonB = createAndSaveLessonWithClassroom(classroom);
            createAndSaveLesson();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("teacherId", teacher.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonA.getId().intValue(),
                    lessonB.getId().intValue()
                )));
        }

        @Test
        void searchLessonsByDateRange() throws Exception {
            // Given
            Lesson lessonA = createAndSaveLessonWithScheduleDate(
                LocalDate.of(2021, 4, 1));
            Lesson lessonB = createAndSaveLessonWithScheduleDate(
                LocalDate.of(2021, 4, 15));
            createAndSaveLessonWithScheduleDate(
                LocalDate.of(2021, 5, 1));

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("sinceDate", "2021-04-01")
                .param("untilDate", "2021-05-01"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonA.getId().intValue(),
                    lessonB.getId().intValue()
                )));
        }

    }

    @Test
    void postLesson() throws Exception {
        // Given
        Classroom classroom = createAndSaveClassroom();

        // When
        LessonDto.Create dto = LessonDto.Create.builder()
            .name("test name")
            .classroomId(classroom.getId())
            .schedule(ScheduleDto.Create.builder()
                .date(stringToLocalDate("1111-11-11"))
                .beginTime(stringToLocalTime("22:22:22"))
                .endTime(stringToLocalTime("00:00:00"))
                .build())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/academy/classroom/lessons")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$.name").value("test name"))
            .andExpect(jsonPath("$.schedule.id").doesNotExist())
            .andExpect(jsonPath("$.schedule.date").value("1111-11-11"))
            .andExpect(jsonPath("$.schedule.beginTime").value("22:22:22"))
            .andExpect(jsonPath("$.schedule.endTime").value("00:00:00"));

        // Document
        actions.andDo(document("lesson-create-example",
            requestFields(
                DOC_FIELD_NAME,
                DOC_FIELD_CLASSROOM_ID,
                DOC_FIELD_SCHEDULE_DATE,
                DOC_FIELD_SCHEDULE_BEGIN_TIME,
                DOC_FIELD_SCHEDULE_END_TIME
            )));
    }

    @Nested
    class PatchLesson {

        @Test
        void patchLesson() throws Exception {
            // Given
            Lesson lesson = Lesson.builder()
                .name("before name")
                .build();

            Schedule schedule = Schedule.builder()
                .date(stringToLocalDate("1111-11-11"))
                .beginTime(stringToLocalTime("22:22:22"))
                .endTime(stringToLocalTime("00:00:00"))
                .build();
            Classroom classroom = aClassroom().withId(null).withAcademy(null);

            lesson.setSchedule(schedule);
            classroom.addLesson(lesson);

            classroomRepository.save(classroom);

            // When
            LessonDto.Update dto = Update.builder()
                .name("test name")
                .schedule(ScheduleDto.Update.builder()
                    .date(stringToLocalDate("2021-03-02"))
                    .beginTime(stringToLocalTime("08:00:00"))
                    .endTime(stringToLocalTime("21:59:59"))
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/academy/classroom/lessons/{id}", lesson.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lesson.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
                .andExpect(jsonPath("$.schedule.id").doesNotExist())
                .andExpect(jsonPath("$.schedule.date").value("2021-03-02"))
                .andExpect(jsonPath("$.schedule.beginTime").value("08:00:00"))
                .andExpect(jsonPath("$.schedule.endTime").value("21:59:59"));

            // Document
            actions.andDo(document("lesson-update-example",
                requestFields(
                    DOC_FIELD_NAME,
                    DOC_FIELD_SCHEDULE_DATE,
                    DOC_FIELD_SCHEDULE_BEGIN_TIME,
                    DOC_FIELD_SCHEDULE_END_TIME
                )));
        }

        @Test
        void patchLesson_LessonNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aLessonUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/academy/classroom/lessons/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteLesson {

        @Test
        void deleteLesson() throws Exception {
            // Given
            Lesson lesson = createAndSaveLesson();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academy/classroom/lessons/{id}", lesson.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(lessonRepository.findById(lesson.getId())).isEmpty();

            // Document
            actions.andDo(document("lesson-delete-example"));
        }

        @Test
        void deleteLesson_LessonNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/academy/classroom/lessons/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private LocalDate stringToLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private LocalTime stringToLocalTime(String date) {
        return LocalTime.parse(date);
    }

    private Classroom createAndSaveClassroom() {
        return classroomRepository.save(Classroom.builder()
            .name("test classroom")
            .build());
    }

    private Classroom createAndSaveClassroomWithAcademy(Academy academy) {
        Classroom classroom = Classroom.builder()
            .name("test name")
            .build();

        classroom.setAcademy(academy);

        return classroomRepository.save(classroom);
    }

    private Academy createAndSaveAcademy() {
        Teacher owner = createAndSaveTeacher();
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .build();
        academy.addTeacher(owner);
        academy.setOwner(owner);
        return academyRepository.save(academy);
    }

    private Lesson createAndSaveLesson() {
        Lesson lesson = Lesson.builder()
            .name("test lesson")
            .build();

        Schedule schedule = aSchedule().withId(null);
        Classroom classroom = aClassroom().withId(null).withAcademy(null);
        lesson.setSchedule(schedule);
        classroom.addLesson(lesson);

        classroomRepository.save(classroom);
        return lesson;
    }

    private Lesson createAndSaveLessonWithClassroom(Classroom classroom) {
        Lesson lesson = Lesson.builder()
            .name("test lesson")
            .build();

        Schedule schedule = aSchedule().withId(null);
        lesson.setSchedule(schedule);
        classroom.addLesson(lesson);

        return lessonRepository.save(lesson);
    }

    private Lesson createAndSaveLessonWithScheduleDate(LocalDate date) {
        Lesson lesson = Lesson.builder()
            .name("test lesson")
            .build();

        Schedule schedule = Schedule.builder()
            .date(date)
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
        Classroom classroom = aClassroom().withId(null).withAcademy(null);
        lesson.setSchedule(schedule);
        classroom.addLesson(lesson);

        classroomRepository.save(classroom);
        return lesson;
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

}
