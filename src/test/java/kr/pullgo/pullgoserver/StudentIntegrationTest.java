package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudentApplyAcademyDto;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudentApplyClassroomDto;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudentRemoveAppliedAcademyDto;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudentRemoveAppliedClassroomDto;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudentUpdateDto;
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
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
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
public class StudentIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("학생 ID");
    private static final FieldDescriptor DOC_FIELD_PARENT_PHONE =
        fieldWithPath("parentPhone").description("부모님 전화번호");
    private static final FieldDescriptor DOC_FIELD_SCHOOL_NAME =
        fieldWithPath("schoolName").description("학교 이름");
    private static final FieldDescriptor DOC_FIELD_SCHOOL_YEAR =
        fieldWithPath("schoolYear").description("학년");
    private static final FieldDescriptor DOC_FIELD_ACCOUNT_USERNAME =
        fieldWithPath("account.username").description("사용자 이름");
    private static final FieldDescriptor DOC_FIELD_ACCOUNT_PASSWORD =
        fieldWithPath("account.password").description("비밀번호");
    private static final FieldDescriptor DOC_FIELD_ACCOUNT_FULL_NAME =
        fieldWithPath("account.fullName").description("실명");
    private static final FieldDescriptor DOC_FIELD_ACCOUNT_PHONE =
        fieldWithPath("account.phone").description("전화번호");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

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
    class GetStudent {

        @Test
        void getStudent() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withUsername("testusername")
                        .withPassword("testpassword")
                        .withFullName("Test FullName")
                        .withPhone("01012345678")
                );
                Student student = entityHelper.generateStudent(it ->
                    it.withAccount(account)
                        .withParentPhone("01098765432")
                        .withSchoolName("test school")
                        .withSchoolYear(1)
                );
                return student.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(get("/students/{id}", studentId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.parentPhone").value("01098765432"))
                .andExpect(jsonPath("$.schoolName").value("test school"))
                .andExpect(jsonPath("$.schoolYear").value(1))
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("testusername"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));

            // Document
            actions.andDo(document("student-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_PARENT_PHONE,
                    DOC_FIELD_SCHOOL_NAME,
                    DOC_FIELD_SCHOOL_YEAR,
                    DOC_FIELD_ACCOUNT_USERNAME,
                    DOC_FIELD_ACCOUNT_FULL_NAME,
                    DOC_FIELD_ACCOUNT_PHONE
                )));
        }

        @Test
        void getStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/students/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchStudents {

        @Test
        void listStudents() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Student studentA = entityHelper.generateStudent();
                Student studentB = entityHelper.generateStudent();

                return new Struct()
                    .withValue("studentAId", studentA.getId())
                    .withValue("studentBId", studentB.getId());
            });
            Long studentAId = given.valueOf("studentAId");
            Long studentBId = given.valueOf("studentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/students"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentAId.intValue(),
                    studentBId.intValue()
                )));

            // Document
            actions.andDo(document("student-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listStudentsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateStudent();
                Student studentA = entityHelper.generateStudent();
                Student studentB = entityHelper.generateStudent();

                return new Struct()
                    .withValue("studentAId", studentA.getId())
                    .withValue("studentBId", studentB.getId());
            });
            Long studentAId = given.valueOf("studentAId");
            Long studentBId = given.valueOf("studentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    studentBId.intValue(),
                    studentAId.intValue()
                )));
        }

        @Test
        void searchStudentsByAcademyId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();

                Student studentA = entityHelper.generateStudent(it -> {
                    academy.addStudent(it);
                    return it;
                });
                Student studentB = entityHelper.generateStudent(it -> {
                    academy.addStudent(it);
                    return it;
                });
                entityHelper.generateStudent();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentAId", studentA.getId())
                    .withValue("studentBId", studentB.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long studentAId = given.valueOf("studentAId");
            Long studentBId = given.valueOf("studentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("academyId", academyId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentAId.intValue(),
                    studentBId.intValue()
                )));

            // Document
            actions.andDo(document("student-search-example",
                requestParameters(
                    parameterWithName("academyId")
                        .description("등록된 학원 ID").optional(),
                    parameterWithName("appliedAcademyId")
                        .description("가입 요청한 학원 ID").optional(),
                    parameterWithName("classroomId")
                        .description("등록된 반 ID").optional(),
                    parameterWithName("appliedClassroomId")
                        .description("가입 요청한 반 ID").optional()
                )));
        }

        @Test
        void searchStudentsByAppliedAcademyId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();

                Student studentA = entityHelper.generateStudent(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                Student studentB = entityHelper.generateStudent(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                entityHelper.generateStudent();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentAId", studentA.getId())
                    .withValue("studentBId", studentB.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long studentAId = given.valueOf("studentAId");
            Long studentBId = given.valueOf("studentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("appliedAcademyId", academyId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentAId.intValue(),
                    studentBId.intValue()
                )));
        }

        @Test
        void searchStudentsByClassroomId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Student studentA = entityHelper.generateStudent(it -> {
                    classroom.addStudent(it);
                    return it;
                });
                Student studentB = entityHelper.generateStudent(it -> {
                    classroom.addStudent(it);
                    return it;
                });
                entityHelper.generateStudent();

                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentAId", studentA.getId())
                    .withValue("studentBId", studentB.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long studentAId = given.valueOf("studentAId");
            Long studentBId = given.valueOf("studentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("classroomId", classroomId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentAId.intValue(),
                    studentBId.intValue()
                )));
        }

        @Test
        void searchStudentsByAppliedClassroomId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Student studentA = entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                Student studentB = entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                entityHelper.generateStudent();

                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentAId", studentA.getId())
                    .withValue("studentBId", studentB.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long studentAId = given.valueOf("studentAId");
            Long studentBId = given.valueOf("studentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("appliedClassroomId", classroomId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentAId.intValue(),
                    studentBId.intValue()
                )));
        }

    }

    @Test
    void postStudent() throws Exception {
        // When
        StudentDto.Create dto = StudentDto.Create.builder()
            .parentPhone("01098765432")
            .schoolName("test school")
            .schoolYear(1)
            .account(AccountDto.Create.builder()
                .username("testusername")
                .password("testpassword")
                .fullName("Test FullName")
                .phone("01012345678")
                .build())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/students")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.parentPhone").value("01098765432"))
            .andExpect(jsonPath("$.schoolName").value("test school"))
            .andExpect(jsonPath("$.schoolYear").value(1))
            .andExpect(jsonPath("$.account.id").doesNotExist())
            .andExpect(jsonPath("$.account.username").value("testusername"))
            .andExpect(jsonPath("$.account.password").doesNotExist())
            .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
            .andExpect(jsonPath("$.account.phone").value("01012345678"));

        // Document
        actions.andDo(document("student-create-example",
            requestFields(
                DOC_FIELD_PARENT_PHONE,
                DOC_FIELD_SCHOOL_NAME,
                DOC_FIELD_SCHOOL_YEAR,
                DOC_FIELD_ACCOUNT_USERNAME,
                DOC_FIELD_ACCOUNT_PASSWORD,
                DOC_FIELD_ACCOUNT_FULL_NAME,
                DOC_FIELD_ACCOUNT_PHONE
            )));
    }

    @Nested
    class PatchStudent {

        @Test
        void patchStudent() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withUsername("testusername")
                        .withPassword("beforePwd")
                        .withFullName("Before FullName")
                        .withPhone("01011112222")
                );
                Student student = entityHelper.generateStudent(it ->
                    it.withAccount(account)
                        .withParentPhone("01099998888")
                        .withSchoolName("before school")
                        .withSchoolYear(3)
                );
                return student.getId();
            });

            // When
            StudentDto.Update dto = StudentDto.Update.builder()
                .parentPhone("01098765432")
                .schoolName("test school")
                .schoolYear(1)
                .account(AccountDto.Update.builder()
                    .password("testpassword")
                    .fullName("Test FullName")
                    .phone("01012345678")
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.parentPhone").value("01098765432"))
                .andExpect(jsonPath("$.schoolName").value("test school"))
                .andExpect(jsonPath("$.schoolYear").value(1))
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("testusername"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));

            // Document
            actions.andDo(document("student-update-example",
                requestFields(
                    DOC_FIELD_PARENT_PHONE.optional(),
                    DOC_FIELD_SCHOOL_NAME.optional(),
                    DOC_FIELD_SCHOOL_YEAR.optional(),
                    DOC_FIELD_ACCOUNT_PASSWORD.optional(),
                    DOC_FIELD_ACCOUNT_FULL_NAME.optional(),
                    DOC_FIELD_ACCOUNT_PHONE.optional()
                )));
        }

        @Test
        void patchStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aStudentUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/students/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteStudent {

        @Test
        void deleteStudent() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Academy joiningAcademy = entityHelper.generateAcademy();
                Academy applyingAcademy = entityHelper.generateAcademy();
                Classroom joiningClassroom = entityHelper.generateClassroom();
                Classroom applyingClassroom = entityHelper.generateClassroom();

                Student student = entityHelper.generateStudent(it -> {
                    joiningAcademy.addStudent(it);
                    joiningClassroom.addStudent(it);
                    it.applyAcademy(applyingAcademy);
                    it.applyClassroom(applyingClassroom);
                    return it;
                });
                return student.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(delete("/students/{id}", studentId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(studentRepository.findById(studentId)).isEmpty();

            // Document
            actions.andDo(document("student-delete-example"));
        }

        @Test
        void deleteStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/students/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class ApplyAcademy {

        @Test
        void applyAcademy() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Student student = entityHelper.generateStudent();
                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentId", student.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aStudentApplyAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("student-apply-academy-example",
                requestFields(
                    fieldWithPath("academyId").description("가입 요청할 학원 ID")
                )));
        }

        @Test
        void applyAcademy_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aStudentApplyAcademyDto());

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                return student.getId();
            });

            // When
            String body = toJson(aStudentApplyAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyAcademy_StudentAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Student student = entityHelper.generateStudent(it -> {
                    academy.addStudent(it);
                    return it;
                });
                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentId", student.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aStudentApplyAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class RemoveAppliedAcademy {

        @Test
        void removeAppliedAcademy() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Student student = entityHelper.generateStudent(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentId", student.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("student-remove-applied-academy-example",
                requestFields(
                    fieldWithPath("academyId").description("가입 요청을 철회할 학원 ID")
                )));
        }

        @Test
        void removeAppliedAcademy_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto());

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                return student.getId();
            });

            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedAcademy_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Student student = entityHelper.generateStudent();
                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentId", student.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class ApplyClassroom {

        @Test
        void applyClassroom() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent();
                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentId", student.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aStudentApplyClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("student-apply-classroom-example",
                requestFields(
                    fieldWithPath("classroomId").description("가입 요청할 반 ID")
                )));
        }

        @Test
        void applyClassroom_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aStudentApplyClassroomDto());

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                return student.getId();
            });

            // When
            String body = toJson(aStudentApplyClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyClassroom_StudentAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent(it -> {
                    classroom.addStudent(it);
                    return it;
                });
                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentId", student.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(aStudentApplyClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class RemoveAppliedClassroom {

        @Test
        void removeAppliedClassroom() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentId", student.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(
                aStudentRemoveAppliedClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("student-remove-applied-classroom-example",
                requestFields(
                    fieldWithPath("classroomId").description("가입 요청을 철회할 반 ID")
                )));
        }

        @Test
        void removeAppliedClassroom_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aStudentRemoveAppliedClassroomDto());

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // Given
            Long studentId = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                return student.getId();
            });

            // When
            String body = toJson(aStudentRemoveAppliedClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", studentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedClassroom_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Student student = entityHelper.generateStudent();
                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("studentId", student.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long studentId = given.valueOf("studentId");

            // When
            String body = toJson(
                aStudentRemoveAppliedClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", studentId)
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

}
