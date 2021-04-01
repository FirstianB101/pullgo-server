package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
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
import java.util.function.Consumer;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ClassroomIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("반 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("반 이름");
    private static final FieldDescriptor DOC_FIELD_ACADEMY_ID =
        fieldWithPath("academyId").description("소속된 학원 ID");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class GetClassroom {

        @Test
        void getClassroom() throws Exception {
            // Given
            Classroom classroom = classroomRepository.save(Classroom.builder()
                .name("test name")
                .build());

            Academy academy = createAndSaveAcademy();
            classroom.setAcademy(academy);
            classroomRepository.save(classroom);

            // When
            ResultActions actions = mockMvc
                .perform(get("/academy/classrooms/{id}", classroom.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroom.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.academyId").value(academy.getId()));

            // Document
            actions.andDo(document("classroom-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_ACADEMY_ID
                )));
        }

        @Test
        void getClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchClassrooms {

        @Test
        void listClassrooms() throws Exception {
            // Given
            Classroom classroomA = createAndSaveClassroom();
            Classroom classroomB = createAndSaveClassroom();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));

            // Document
            actions.andDo(document("classroom-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listClassroomsWithPaging() throws Exception {
            // Given
            createAndSaveClassroom();
            Classroom classroomA = createAndSaveClassroom();
            Classroom classroomB = createAndSaveClassroom();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    classroomB.getId().intValue(),
                    classroomA.getId().intValue()
                )));
        }

        @Test
        void searchClassroomsByAcademyId() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();

            Classroom classroomA = createAndSaveClassroomWithAcademy(academy);
            Classroom classroomB = createAndSaveClassroomWithAcademy(academy);
            createAndSaveClassroom();

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("academyId", academy.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));

            // Document
            actions.andDo(document("classroom-search-example",
                requestParameters(
                    parameterWithName("academyId")
                        .description("소속된 학원 ID").optional(),
                    parameterWithName("studentId")
                        .description("반 수강생 ID").optional(),
                    parameterWithName("applyingStudentId")
                        .description("반에 가입 요청한 학생 ID").optional(),
                    parameterWithName("teacherId")
                        .description("반 선생님 ID").optional(),
                    parameterWithName("applyingTeacherId")
                        .description("반에 가입 요청한 선생님 ID").optional(),
                    parameterWithName("nameLike")
                        .description("유사한 반 이름").optional()
                )));
        }

        @Test
        void searchClassroomsByStudentId() throws Exception {
            // Given
            Classroom classroomA = createAndSaveClassroom();
            Classroom classroomB = createAndSaveClassroom();
            createAndSaveClassroom();

            Student student = createAndSaveStudent();
            student.applyClassroom(classroomA);
            student.applyClassroom(classroomB);
            classroomA.acceptStudent(student);
            classroomB.acceptStudent(student);

            classroomRepository.save(classroomA);
            classroomRepository.save(classroomB);

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("studentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));
        }

        @Test
        void searchClassroomsByApplyingStudentId() throws Exception {
            // Given
            Classroom classroomA = createAndSaveClassroom();
            Classroom classroomB = createAndSaveClassroom();
            createAndSaveClassroom();

            Student student = createAndSaveStudent();
            student.applyClassroom(classroomA);
            student.applyClassroom(classroomB);

            studentRepository.save(student);

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("applyingStudentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));
        }

        @Test
        void searchClassroomsByTeacherId() throws Exception {
            // Given
            Classroom classroomA = createAndSaveClassroom();
            Classroom classroomB = createAndSaveClassroom();
            createAndSaveClassroom();

            Teacher teacher = createAndSaveTeacher();
            teacher.applyClassroom(classroomA);
            teacher.applyClassroom(classroomB);
            classroomA.acceptTeacher(teacher);
            classroomB.acceptTeacher(teacher);

            classroomRepository.save(classroomA);
            classroomRepository.save(classroomB);

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("teacherId", teacher.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));
        }

        @Test
        void searchClassroomsByApplyingTeacherId() throws Exception {
            // Given
            Classroom classroomA = createAndSaveClassroom();
            Classroom classroomB = createAndSaveClassroom();
            createAndSaveClassroom();

            Teacher teacher = createAndSaveTeacher();
            teacher.applyClassroom(classroomA);
            teacher.applyClassroom(classroomB);

            teacherRepository.save(teacher);

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("applyingTeacherId", teacher.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));
        }

        @Test
        void searchClassroomsByNameLike() throws Exception {
            // Given
            Classroom classroomA = createAndSaveClassroomWithName("컴퓨터네트워크 월수 6시 최웅철");
            Classroom classroomB = createAndSaveClassroomWithName("네트워크의 이해 화목 3시 이동호");
            createAndSaveClassroomWithName("운영체제 화목 12시 안우현");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("nameLike", "네트워크"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomA.getId().intValue(),
                    classroomB.getId().intValue()
                )));
        }

    }

    @Test
    void postClassroom() throws Exception {
        // Given
        Academy academy = createAndSaveAcademy();
        Teacher creator = createAndSaveTeacher();

        // When
        ClassroomDto.Create dto = ClassroomDto.Create.builder()
            .name("test name")
            .academyId(academy.getId())
            .creatorId(creator.getId())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/academy/classrooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        MvcResult mvcResult = actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("test name"))
            .andExpect(jsonPath("$.academyId").value(academy.getId()))
            .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ClassroomDto.Result resultDto = fromJson(responseBody, ClassroomDto.Result.class);

        withTransaction(status -> {
            Classroom classroom = classroomRepository.findById(resultDto.getId()).orElseThrow();

            assertThat(classroom.getTeachers()).extracting("id")
                .contains(creator.getId());
        });

        // Document
        actions.andDo(document("classroom-create-example",
            requestFields(
                DOC_FIELD_NAME,
                DOC_FIELD_ACADEMY_ID,
                fieldWithPath("creatorId").description("반을 생성한 선생님 ID")
            )));
    }

    @Nested
    class PatchClassroom {

        @Test
        void patchClassroom() throws Exception {
            // Given
            Classroom classroom = classroomRepository.save(Classroom.builder()
                .name("before name")
                .build());

            Academy academy = createAndSaveAcademy();
            classroom.setAcademy(academy);
            classroomRepository.save(classroom);

            // When
            ClassroomDto.Update dto = ClassroomDto.Update.builder()
                .name("test name")
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/academy/classrooms/{id}", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroom.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.academyId").value(academy.getId()));

            // Document
            actions.andDo(document("classroom-update-example",
                requestFields(
                    DOC_FIELD_NAME.optional()
                )));
        }

        @Test
        void patchClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(classroomUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/academy/classrooms/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteClassroom {

        @Test
        void deleteClassroom() throws Exception {
            // Given
            Long classroomId = createAndSaveClassroom().getId();

            Long teacherAId = createAndSaveTeacher().getId();
            addTeacher(classroomId, teacherAId);

            Long teacherBId = createAndSaveTeacher().getId();
            addApplyingTeacher(classroomId, teacherBId);

            Long studentAId = createAndSaveStudent().getId();
            addStudent(classroomId, studentAId);

            Long studentBId = createAndSaveStudent().getId();
            addApplyingStudent(classroomId, studentBId);

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academy/classrooms/{id}", classroomId));

            // Then
            actions
                .andExpect(status().isNoContent());

            assertThat(classroomRepository.findById(classroomId)).isEmpty();

            assertThat(teacherRepository.findById(teacherAId)).isNotEmpty();
            assertThat(teacherRepository.findById(teacherBId)).isNotEmpty();

            assertThat(studentRepository.findById(studentAId)).isNotEmpty();
            assertThat(studentRepository.findById(studentBId)).isNotEmpty();

            // Document
            actions.andDo(document("classroom-delete-example"));
        }

        @Test
        void deleteClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/academy/classrooms/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class AcceptTeacher {

        @Test
        void acceptTeacher() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Teacher teacher = createAndSaveTeacher();

            teacher.applyClassroom(classroom);
            teacherRepository.save(teacher);

            // When
            String body = toJson(acceptTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("classroom-accept-teacher-example",
                requestFields(
                    fieldWithPath("teacherId").description("승인할 선생님 ID")
                )));
        }

        @Test
        void acceptTeacher_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(acceptTeacherDto());

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(acceptTeacherDtoWithTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptTeacher_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(acceptTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class KickTeacher {

        @Test
        void kickTeacher() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Teacher teacher = createAndSaveTeacher();

            teacher.applyClassroom(classroom);
            classroom.acceptTeacher(teacher);
            classroomRepository.save(classroom);

            // When
            String body = toJson(kickTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("classroom-kick-teacher-example",
                requestFields(
                    fieldWithPath("teacherId").description("추방할 선생님 ID")
                )));
        }

        @Test
        void kickTeacher_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(kickTeacherDto());

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(kickTeacherDtoWithTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickTeacher_TeacherNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(kickTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class AcceptStudent {

        @Test
        void acceptStudent() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Student student = createAndSaveStudent();

            student.applyClassroom(classroom);
            studentRepository.save(student);

            // When
            String body = toJson(acceptStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("classroom-accept-student-example",
                requestFields(
                    fieldWithPath("studentId").description("승인할 학생 ID")
                )));
        }

        @Test
        void acceptStudent_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(acceptStudentDto());

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(acceptStudentDtoWithStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptStudent_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Student student = createAndSaveStudent();

            // When
            String body = toJson(acceptStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class KickStudent {

        @Test
        void kickStudent() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Student student = createAndSaveStudent();

            student.applyClassroom(classroom);
            classroom.acceptStudent(student);
            classroomRepository.save(classroom);

            // When
            String body = toJson(kickStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("classroom-kick-student-example",
                requestFields(
                    fieldWithPath("studentId").description("추방할 학생 ID")
                )));
        }

        @Test
        void kickStudent_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(kickStudentDto());

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(kickStudentDtoWithStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickStudent_StudentNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Classroom classroom = createAndSaveClassroom();
            Student student = createAndSaveStudent();

            // When
            String body = toJson(kickStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", classroom.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String responseBody, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, clazz);
    }

    private void withTransaction(Consumer<TransactionStatus> callback) {
        transactionTemplate.executeWithoutResult(callback);
    }

    private Academy createAndSaveAcademy() {
        return academyRepository.save(
            Academy.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .build());
    }

    private Classroom createAndSaveClassroom() {
        Classroom classroom = Classroom.builder()
            .name("test name")
            .build();

        Academy academy = createAndSaveAcademy();
        classroom.setAcademy(academy);

        return classroomRepository.save(classroom);
    }

    private Classroom createAndSaveClassroomWithName(String name) {
        Classroom classroom = Classroom.builder()
            .name(name)
            .build();

        Academy academy = createAndSaveAcademy();
        classroom.setAcademy(academy);

        return classroomRepository.save(classroom);
    }

    private Classroom createAndSaveClassroomWithAcademy(Academy academy) {
        Classroom classroom = Classroom.builder()
            .name("test name")
            .build();

        classroom.setAcademy(academy);

        return classroomRepository.save(classroom);
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

    private void addTeacher(Long classroomId, Long teacherId) {
        withTransaction(status -> {
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();

            classroom.addTeacher(teacher);
        });
    }

    private void addApplyingTeacher(Long classroomId, Long teacherId) {
        withTransaction(status -> {
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();

            teacher.applyClassroom(classroom);
        });
    }

    private void addStudent(Long classroomId, Long studentId) {
        withTransaction(status -> {
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();
            Student student = studentRepository.findById(studentId).orElseThrow();

            student.applyClassroom(classroom);
            classroom.acceptStudent(student);
        });
    }

    private void addApplyingStudent(Long classroomId, Long studentId) {
        withTransaction(status -> {
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();
            Student student = studentRepository.findById(studentId).orElseThrow();

            student.applyClassroom(classroom);
        });
    }

    private ClassroomDto.Update classroomUpdateDto() {
        return ClassroomDto.Update.builder()
            .name("test name")
            .build();
    }

    private ClassroomDto.AcceptTeacher acceptTeacherDto() {
        return ClassroomDto.AcceptTeacher.builder()
            .teacherId(0L)
            .build();
    }

    private ClassroomDto.AcceptTeacher acceptTeacherDtoWithTeacherId(Long id) {
        return ClassroomDto.AcceptTeacher.builder()
            .teacherId(id)
            .build();
    }

    private ClassroomDto.KickTeacher kickTeacherDto() {
        return ClassroomDto.KickTeacher.builder()
            .teacherId(0L)
            .build();
    }

    private ClassroomDto.KickTeacher kickTeacherDtoWithTeacherId(Long id) {
        return ClassroomDto.KickTeacher.builder()
            .teacherId(id)
            .build();
    }

    private ClassroomDto.AcceptStudent acceptStudentDto() {
        return ClassroomDto.AcceptStudent.builder()
            .studentId(0L)
            .build();
    }

    private ClassroomDto.AcceptStudent acceptStudentDtoWithStudentId(Long id) {
        return ClassroomDto.AcceptStudent.builder()
            .studentId(id)
            .build();
    }

    private ClassroomDto.KickStudent kickStudentDto() {
        return ClassroomDto.KickStudent.builder()
            .studentId(0L)
            .build();
    }

    private ClassroomDto.KickStudent kickStudentDtoWithStudentId(Long id) {
        return ClassroomDto.KickStudent.builder()
            .studentId(id)
            .build();
    }

}
