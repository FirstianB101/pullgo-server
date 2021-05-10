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
import java.util.function.Consumer;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
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
    private AccountRepository accountRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

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
    class GetStudent {

        @Test
        void getStudent() throws Exception {
            // Given
            Student student = studentRepository.save(Student.builder()
                .parentPhone("01098765432")
                .schoolName("test school")
                .schoolYear(1)
                .build());
            Account account = accountRepository.save(Account.builder()
                .username("testusername")
                .password("testpassword")
                .fullName("Test FullName")
                .phone("01012345678")
                .build());
            student.setAccount(account);
            studentRepository.save(student);

            // When
            ResultActions actions = mockMvc.perform(get("/students/{id}", student.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
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
            Student studentA = createAndSaveStudent();
            Student studentB = createAndSaveStudent();

            // When
            ResultActions actions = mockMvc.perform(get("/students"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentA.getId().intValue(),
                    studentB.getId().intValue()
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
            createAndSaveStudent();
            Student studentA = createAndSaveStudent();
            Student studentB = createAndSaveStudent();

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
                    studentB.getId().intValue(),
                    studentA.getId().intValue()
                )));
        }

        @Test
        void searchStudentsByAcademyId() throws Exception {
            // Given
            Student studentA = createAndSaveStudent();
            Student studentB = createAndSaveStudent();
            createAndSaveStudent();

            Academy academy = createAndSaveAcademy();
            studentA.applyAcademy(academy);
            studentB.applyAcademy(academy);
            academy.acceptStudent(studentA);
            academy.acceptStudent(studentB);

            academyRepository.save(academy);

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("academyId", academy.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentA.getId().intValue(),
                    studentB.getId().intValue()
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
            Student studentA = createAndSaveStudent();
            Student studentB = createAndSaveStudent();
            createAndSaveStudent();

            Academy academy = createAndSaveAcademy();
            studentA.applyAcademy(academy);
            studentB.applyAcademy(academy);

            studentRepository.save(studentA);
            studentRepository.save(studentB);

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("appliedAcademyId", academy.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentA.getId().intValue(),
                    studentB.getId().intValue()
                )));
        }

        @Test
        void searchStudentsByClassroomId() throws Exception {
            // Given
            Student studentA = createAndSaveStudent();
            Student studentB = createAndSaveStudent();
            createAndSaveStudent();

            Classroom classroom = createAndSaveClassroom();
            studentA.applyClassroom(classroom);
            studentB.applyClassroom(classroom);
            classroom.acceptStudent(studentA);
            classroom.acceptStudent(studentB);

            classroomRepository.save(classroom);

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("classroomId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentA.getId().intValue(),
                    studentB.getId().intValue()
                )));
        }

        @Test
        void searchStudentsByAppliedClassroomId() throws Exception {
            // Given
            Student studentA = createAndSaveStudent();
            Student studentB = createAndSaveStudent();
            createAndSaveStudent();

            Classroom classroom = createAndSaveClassroom();
            studentA.applyClassroom(classroom);
            studentB.applyClassroom(classroom);

            studentRepository.save(studentA);
            studentRepository.save(studentB);

            // When
            ResultActions actions = mockMvc.perform(get("/students")
                .param("appliedClassroomId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    studentA.getId().intValue(),
                    studentB.getId().intValue()
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
            Student student = studentRepository.save(Student.builder()
                .parentPhone("01099998888")
                .schoolName("before school")
                .schoolYear(3)
                .build());
            Account account = accountRepository.save(Account.builder()
                .username("testusername")
                .password("beforePwd")
                .fullName("Before FullName")
                .phone("01011112222")
                .build());
            student.setAccount(account);
            studentRepository.save(student);

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

            ResultActions actions = mockMvc.perform(patch("/students/{id}", student.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
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
            Long studentId = createAndSaveStudent().getId();

            Long academyAId = createAndSaveAcademy().getId();
            addAcademy(studentId, academyAId);

            Long academyBId = createAndSaveAcademy().getId();
            addAppliedAcademy(studentId, academyBId);

            Long classroomAId = createAndSaveClassroom().getId();
            addClassroom(studentId, classroomAId);

            Long classroomBId = createAndSaveClassroom().getId();
            addAppliedClassroom(studentId, classroomBId);

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
            Student student = createAndSaveStudent();
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(aStudentApplyAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", student.getId())
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
            Student student = createAndSaveStudent();

            // When
            String body = toJson(aStudentApplyAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyAcademy_StudentAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Student student = createAndSaveStudent();
            Academy academy = createAndSaveAcademy();

            student.applyAcademy(academy);
            academy.acceptStudent(student);
            academyRepository.save(academy);

            // When
            String body = toJson(aStudentApplyAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", student.getId())
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
            Student student = createAndSaveStudent();
            Academy academy = createAndSaveAcademy();

            student.applyAcademy(academy);
            studentRepository.save(student);

            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", student.getId())
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
            Student student = createAndSaveStudent();

            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedAcademy_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Student student = createAndSaveStudent();
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(aStudentRemoveAppliedAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", student.getId())
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
            Student student = createAndSaveStudent();
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(aStudentApplyClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", student.getId())
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
            Student student = createAndSaveStudent();

            // When
            String body = toJson(aStudentApplyClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyClassroom_StudentAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Student student = createAndSaveStudent();
            Classroom classroom = createAndSaveClassroom();

            student.applyClassroom(classroom);
            classroom.acceptStudent(student);
            classroomRepository.save(classroom);

            // When
            String body = toJson(aStudentApplyClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", student.getId())
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
            Student student = createAndSaveStudent();
            Classroom classroom = createAndSaveClassroom();

            student.applyClassroom(classroom);
            studentRepository.save(student);

            // When
            String body = toJson(
                aStudentRemoveAppliedClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", student.getId())
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
            Student student = createAndSaveStudent();

            // When
            String body = toJson(aStudentRemoveAppliedClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedClassroom_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Student student = createAndSaveStudent();
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(
                aStudentRemoveAppliedClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", student.getId())
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

    private void withTransaction(Consumer<TransactionStatus> callback) {
        transactionTemplate.executeWithoutResult(callback);
    }

    private Student createAndSaveStudent() {
        Student student = studentRepository.save(Student.builder()
            .parentPhone("01098765432")
            .schoolName("test school")
            .schoolYear(1)
            .build());
        Account account = accountRepository.save(Account.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build());
        student.setAccount(account);
        studentRepository.save(student);
        return student;
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
        Academy academy = createAndSaveAcademy();
        Classroom classroom = classroomRepository.save(
            Classroom.builder()
                .name("test name")
                .build());
        classroom.setAcademy(academy);
        classroomRepository.save(classroom);
        return classroom;
    }

    private void addAcademy(Long studentId, Long academyId) {
        withTransaction(status -> {
            Student student = studentRepository.findById(studentId).orElseThrow();
            Academy academy = academyRepository.findById(academyId).orElseThrow();

            student.applyAcademy(academy);
            academy.acceptStudent(student);
        });
    }

    private void addAppliedAcademy(Long studentId, Long academyId) {
        withTransaction(status -> {
            Student student = studentRepository.findById(studentId).orElseThrow();
            Academy academy = academyRepository.findById(academyId).orElseThrow();

            student.applyAcademy(academy);
        });
    }

    private void addClassroom(Long studentId, Long classroomId) {
        withTransaction(status -> {
            Student student = studentRepository.findById(studentId).orElseThrow();
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();

            student.applyClassroom(classroom);
            classroom.acceptStudent(student);
        });
    }

    private void addAppliedClassroom(Long studentId, Long classroomId) {
        withTransaction(status -> {
            Student student = studentRepository.findById(studentId).orElseThrow();
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();

            student.applyClassroom(classroom);
        });
    }

}
