package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.LessonHelper.aLessonUpdateDto;
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
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.LessonDto.Update;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
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
    class GetLesson {

        @Test
        void getLesson() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Schedule schedule = entityHelper.generateSchedule(it ->
                    it.withDate(LocalDate.of(1111, 11, 11))
                        .withBeginTime(LocalTime.of(22, 22, 22))
                        .withEndTime(LocalTime.of(0, 0, 0))
                );
                Lesson lesson = entityHelper.generateLesson(it ->
                    it.withName("test name")
                        .withSchedule(schedule)
                );

                return new Struct()
                    .withValue("lessonId", lesson.getId())
                    .withValue("classroomId", lesson.getClassroom().getId());
            });
            Long lessonId = given.valueOf("lessonId");
            Long classroomId = given.valueOf("classroomId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/academy/classroom/lessons/{id}", lessonId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.classroomId").value(classroomId))
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
            Struct given = trxHelper.doInTransaction(() -> {
                Lesson lessonA = entityHelper.generateLesson();
                Lesson lessonB = entityHelper.generateLesson();

                return new Struct()
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonAId.intValue(),
                    lessonBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateLesson();
                Lesson lessonA = entityHelper.generateLesson();
                Lesson lessonB = entityHelper.generateLesson();

                return new Struct()
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

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
                    lessonBId.intValue(),
                    lessonAId.intValue()
                )));
        }

        @Test
        void searchLessonsByClassroomId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Lesson lessonA = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                Lesson lessonB = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                entityHelper.generateLesson();

                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("classroomId", classroomId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonAId.intValue(),
                    lessonBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Lesson lessonA = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                Lesson lessonB = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                entityHelper.generateLesson();

                return new Struct()
                    .withValue("academyId", classroom.getAcademy().getId())
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("academyId", academyId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonAId.intValue(),
                    lessonBId.intValue()
                )));
        }

        @Test
        void searchLessonsByStudentId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent(it -> {
                    classroom.addStudent(it);
                    return it;
                });

                Lesson lessonA = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                Lesson lessonB = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                entityHelper.generateLesson();

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("studentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonAId.intValue(),
                    lessonBId.intValue()
                )));
        }

        @Test
        void searchLessonsByTeacherId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    classroom.addTeacher(it);
                    return it;
                });

                Lesson lessonA = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                Lesson lessonB = entityHelper.generateLesson(it -> it.withClassroom(classroom));
                entityHelper.generateLesson();

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("teacherId", teacherId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonAId.intValue(),
                    lessonBId.intValue()
                )));
        }

        @Test
        void searchLessonsByDateRange() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Schedule scheduleA = entityHelper.generateSchedule(it ->
                    it.withDate(LocalDate.of(2021, 4, 1))
                );
                Lesson lessonA = entityHelper.generateLesson(it -> it.withSchedule(scheduleA));

                Schedule scheduleB = entityHelper.generateSchedule(it ->
                    it.withDate(LocalDate.of(2021, 4, 15))
                );
                Lesson lessonB = entityHelper.generateLesson(it -> it.withSchedule(scheduleB));

                Schedule scheduleC = entityHelper.generateSchedule(it ->
                    it.withDate(LocalDate.of(2021, 5, 1))
                );
                entityHelper.generateLesson(it -> it.withSchedule(scheduleC));

                return new Struct()
                    .withValue("lessonAId", lessonA.getId())
                    .withValue("lessonBId", lessonB.getId());
            });
            Long lessonAId = given.valueOf("lessonAId");
            Long lessonBId = given.valueOf("lessonBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons")
                .param("sinceDate", "2021-04-01")
                .param("untilDate", "2021-05-01"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    lessonAId.intValue(),
                    lessonBId.intValue()
                )));
        }

    }

    @Test
    void postLesson() throws Exception {
        // Given
        Long classroomId = trxHelper.doInTransaction(() -> {
            Classroom classroom = entityHelper.generateClassroom();
            return classroom.getId();
        });

        // When
        LessonDto.Create dto = LessonDto.Create.builder()
            .name("test name")
            .classroomId(classroomId)
            .schedule(ScheduleDto.Create.builder()
                .date(LocalDate.of(1111, 11, 11))
                .beginTime(LocalTime.of(22, 22, 22))
                .endTime(LocalTime.of(0, 0, 0))
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
            .andExpect(jsonPath("$.classroomId").value(classroomId))
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
            Struct given = trxHelper.doInTransaction(() -> {
                Schedule schedule = entityHelper.generateSchedule(it ->
                    it.withDate(LocalDate.of(1111, 11, 11))
                        .withBeginTime(LocalTime.of(22, 22, 22))
                        .withEndTime(LocalTime.of(0, 0, 0))
                );
                Lesson lesson = entityHelper.generateLesson(it ->
                    it.withName("before name")
                        .withSchedule(schedule)
                );
                return new Struct()
                    .withValue("lessonId", lesson.getId())
                    .withValue("classroomId", lesson.getClassroom().getId());
            });
            Long lessonId = given.valueOf("lessonId");
            Long classroomId = given.valueOf("classroomId");

            // When
            LessonDto.Update dto = Update.builder()
                .name("test name")
                .schedule(ScheduleDto.Update.builder()
                    .date(LocalDate.of(2021, 3, 2))
                    .beginTime(LocalTime.of(8, 0, 0))
                    .endTime(LocalTime.of(21, 59, 59))
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/academy/classroom/lessons/{id}", lessonId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.classroomId").value(classroomId))
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
            Long lessonId = trxHelper.doInTransaction(() -> {
                Lesson lesson = entityHelper.generateLesson();
                return lesson.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academy/classroom/lessons/{id}", lessonId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(lessonRepository.findById(lessonId)).isEmpty();

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

}
