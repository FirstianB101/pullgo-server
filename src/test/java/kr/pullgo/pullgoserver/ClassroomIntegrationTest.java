package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroomAcceptStudentDto;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroomAcceptTeacherDto;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroomKickStudentDto;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroomKickTeacherDto;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroomUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.helper.AuthHelper;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
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
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ClassroomIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("반 ID");
    private static final FieldDescriptor DOC_FIELD_CREATOR =
        subsectionWithPath("creator")
            .type("creator").description("생성한 선생님");
    private static final FieldDescriptor DOC_FIELD_CREATOR_ID =
        fieldWithPath("creatorId").description("생성한 선생님 ID");
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
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

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

    @Test
    void postClassroom() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Academy academy = entityHelper.generateAcademy();
            Teacher creator = academy.getOwner();
            String token = authHelper.generateToken(it -> creator.getAccount());
            return new Struct()
                .withValue("token", token)
                .withValue("academyId", academy.getId())
                .withValue("creator", creator);
        });
        String token = given.valueOf("token");
        Long academyId = given.valueOf("academyId");
        Teacher creator = given.valueOf("creator");

        // When
        ClassroomDto.Create dto = ClassroomDto.Create.builder()
            .name("test name")
            .academyId(academyId)
            .creatorId(creator.getId())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/academy/classrooms")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(body));

        // Then
        MvcResult mvcResult = actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("test name"))
            .andExpect(jsonPath("$.creator.id").value(creator.getId()))
            .andExpect(jsonPath("$.academyId").value(academyId))
            .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ClassroomDto.Result resultDto = fromJson(responseBody, ClassroomDto.Result.class);

        List<Long> teacherIds = trxHelper.doInTransaction(() -> {
            Classroom classroom = classroomRepository.findById(resultDto.getId()).orElseThrow();
            return classroom.getTeachers().stream()
                .map(Teacher::getId)
                .collect(Collectors.toList());
        });
        assertThat(teacherIds).contains(creator.getId());

        // Document
        actions.andDo(document("classroom-create-example",
            requestFields(
                DOC_FIELD_NAME,
                DOC_FIELD_CREATOR_ID,
                DOC_FIELD_ACADEMY_ID
            )));
    }

    @Nested
    class SearchClassrooms {

        @Test
        void listClassrooms() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroomA = entityHelper.generateClassroom();
                Classroom classroomB = entityHelper.generateClassroom();

                return new Struct()
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateClassroom();
                Classroom classroomA = entityHelper.generateClassroom();
                Classroom classroomB = entityHelper.generateClassroom();

                return new Struct()
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

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
                    classroomBId.intValue(),
                    classroomAId.intValue()
                )));
        }

        @Test
        void searchClassroomsByAcademyId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();

                Classroom classroomA = entityHelper
                    .generateClassroom(it -> it.withAcademy(academy));
                Classroom classroomB = entityHelper
                    .generateClassroom(it -> it.withAcademy(academy));
                entityHelper.generateClassroom();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("academyId", academyId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();

                Classroom classroomA = entityHelper.generateClassroom(it -> {
                    it.addStudent(student);
                    return it;
                });
                Classroom classroomB = entityHelper.generateClassroom(it -> {
                    it.addStudent(student);
                    return it;
                });
                entityHelper.generateClassroom();

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("studentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
                )));
        }

        @Test
        void searchClassroomsByApplyingStudentId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroomA = entityHelper.generateClassroom();
                Classroom classroomB = entityHelper.generateClassroom();
                entityHelper.generateClassroom();

                Student student = entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroomA);
                    it.applyClassroom(classroomB);
                    return it;
                });

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("applyingStudentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
                )));
        }

        @Test
        void searchClassroomsByTeacherId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();

                Classroom classroomA = entityHelper.generateClassroom(it -> {
                    it.addTeacher(teacher);
                    return it;
                });
                Classroom classroomB = entityHelper.generateClassroom(it -> {
                    it.addTeacher(teacher);
                    return it;
                });
                entityHelper.generateClassroom();

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("teacherId", teacherId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
                )));
        }

        @Test
        void searchClassroomsByApplyingTeacherId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroomA = entityHelper.generateClassroom();
                Classroom classroomB = entityHelper.generateClassroom();
                entityHelper.generateClassroom();

                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroomA);
                    it.applyClassroom(classroomB);
                    return it;
                });

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("applyingTeacherId", teacherId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
                )));
        }

        @Test
        void searchClassroomsByNameLike() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroomA = entityHelper.generateClassroom(it ->
                    it.withName("컴퓨터네트워크 월수 6시 최웅철")
                );
                Classroom classroomB = entityHelper.generateClassroom(it ->
                    it.withName("네트워크의 이해 화목 3시 이동호")
                );
                entityHelper.generateClassroom(it ->
                    it.withName("운영체제 화목 12시 안우현")
                );

                return new Struct()
                    .withValue("classroomAId", classroomA.getId())
                    .withValue("classroomBId", classroomB.getId());
            });
            Long classroomAId = given.valueOf("classroomAId");
            Long classroomBId = given.valueOf("classroomBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academy/classrooms")
                .param("nameLike", "네트워크"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    classroomAId.intValue(),
                    classroomBId.intValue()
                )));
        }

    }

    @Nested
    class GetClassroom {

        @Test
        void getClassroom() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Classroom classroom = entityHelper.generateClassroom(it ->
                    it.withName("컴퓨터네트워크 최웅철 (월수금)")
                        .withAcademy(academy)
                );

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("classroomId", classroom.getId())
                    .withValue("creator", classroom.getCreator());
            });
            Long academyId = given.valueOf("academyId");
            Long classroomId = given.valueOf("classroomId");
            Teacher creator = given.valueOf("creator");

            // When
            ResultActions actions = mockMvc
                .perform(get("/academy/classrooms/{id}", classroomId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroomId))
                .andExpect(jsonPath("$.creator.id").value(creator.getId()))
                .andExpect(jsonPath("$.name").value("컴퓨터네트워크 최웅철 (월수금)"))
                .andExpect(jsonPath("$.academyId").value(academyId));

            // Document
            actions.andDo(document("classroom-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_CREATOR,
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
    class PatchClassroom {

        @Test
        void patchClassroom() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Classroom classroom = entityHelper.generateClassroom(it ->
                    it.withAcademy(academy)
                        .withName("컴퓨터네트워크 최웅철 (월수금)")
                );
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("academyId", academy.getId())
                    .withValue("creator", classroom.getCreator())
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long academyId = given.valueOf("academyId");
            Teacher creator = given.valueOf("creator");
            Long classroomId = given.valueOf("classroomId");

            // When
            ClassroomDto.Update dto = ClassroomDto.Update.builder()
                .name("컴퓨터네트워크 최웅철 (화목)")
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/academy/classrooms/{id}", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroomId))
                .andExpect(jsonPath("$.name").value("컴퓨터네트워크 최웅철 (화목)"))
                .andExpect(jsonPath("$.creator.id").value(creator.getId()))
                .andExpect(jsonPath("$.academyId").value(academyId));

            // Document
            actions.andDo(document("classroom-update-example",
                requestFields(
                    DOC_FIELD_NAME.optional()
                )));
        }

        @Test
        void patchClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aClassroomUpdateDto());

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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Teacher joinedTeacher = entityHelper.generateTeacher(it -> {
                    classroom.addTeacher(it);
                    return it;
                });
                Student joinedStudent = entityHelper.generateStudent(it -> {
                    classroom.addStudent(it);
                    return it;
                });

                Teacher appliedTeacher = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                Student appliedStudent = entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("joinedTeacherId", joinedTeacher.getId())
                    .withValue("joinedStudentId", joinedStudent.getId())
                    .withValue("appliedTeacherId", appliedTeacher.getId())
                    .withValue("appliedStudentId", appliedStudent.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");
            Long joinedTeacherId = given.valueOf("joinedTeacherId");
            Long joinedStudentId = given.valueOf("joinedStudentId");
            Long appliedTeacherId = given.valueOf("appliedTeacherId");
            Long appliedStudentId = given.valueOf("appliedStudentId");

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academy/classrooms/{id}", classroomId)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent());

            assertThat(classroomRepository.existsById(classroomId)).isFalse();

            assertThat(teacherRepository.existsById(joinedTeacherId)).isTrue();
            assertThat(studentRepository.existsById(joinedStudentId)).isTrue();

            assertThat(teacherRepository.existsById(appliedTeacherId)).isTrue();
            assertThat(studentRepository.existsById(appliedStudentId)).isTrue();

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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });

                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");

            // When
            String body = toJson(aClassroomAcceptTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            String body = toJson(aClassroomAcceptTeacherDto());

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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomAcceptTeacherDto().withTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptTeacher_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Teacher teacher = entityHelper.generateTeacher();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());

                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");

            // When
            String body = toJson(aClassroomAcceptTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-teacher", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                Classroom classroom = entityHelper.generateClassroom(it -> {
                    it.addTeacher(teacher);
                    return it;
                });
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId())
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long teacherId = given.valueOf("teacherId");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomKickTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            String body = toJson(aClassroomKickTeacherDto());

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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");
            // When
            String body = toJson(aClassroomKickTeacherDto().withTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickTeacher_TeacherNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId())
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long teacherId = given.valueOf("teacherId");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomKickTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-teacher", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Student student = entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentId", student.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aClassroomAcceptStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            String body = toJson(aClassroomAcceptStudentDto());

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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomAcceptStudentDto().withStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptStudent_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentId", student.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aClassroomAcceptStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/accept-student", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                Classroom classroom = entityHelper.generateClassroom(it -> {
                    it.addStudent(student);
                    return it;
                });
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("studentId", student.getId())
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long studentId = given.valueOf("studentId");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomKickStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            String body = toJson(aClassroomKickStudentDto());

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
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomKickStudentDto().withStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickStudent_StudentNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                Classroom classroom = entityHelper.generateClassroom();
                String token = authHelper.generateToken(it -> classroom.getCreator().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("studentId", student.getId())
                    .withValue("classroomId", classroom.getId());
            });
            String token = given.valueOf("token");
            Long studentId = given.valueOf("studentId");
            Long classroomId = given.valueOf("classroomId");

            // When
            String body = toJson(aClassroomKickStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academy/classrooms/{id}/kick-student", classroomId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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

}
