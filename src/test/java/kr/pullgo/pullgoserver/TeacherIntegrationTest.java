package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherApplyAcademyDto;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherApplyClassroomDto;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherRemoveAppliedAcademyDto;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherRemoveAppliedClassroomDto;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherUpdateDto;
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
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class TeacherIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("선생님 ID");
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
    private TeacherRepository teacherRepository;

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
    class GetTeacher {

        @Test
        void getTeacher() throws Exception {
            // Given
            Teacher teacher = teacherRepository.save(new Teacher());
            Account account = accountRepository.save(Account.builder()
                .username("testusername")
                .password("testpassword")
                .fullName("Test FullName")
                .phone("01012345678")
                .build());
            teacher.setAccount(account);
            teacherRepository.save(teacher);

            // When
            ResultActions actions = mockMvc.perform(get("/teachers/{id}", teacher.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teacher.getId()))
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("testusername"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));

            // Document
            actions.andDo(document("teacher-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_ACCOUNT_USERNAME,
                    DOC_FIELD_ACCOUNT_FULL_NAME,
                    DOC_FIELD_ACCOUNT_PHONE
                )));
        }

        @Test
        void getTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/teachers/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchTeachers {

        @Test
        void listTeachers() throws Exception {
            // Given
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();

            // When
            ResultActions actions = mockMvc.perform(get("/teachers"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherA.getId().intValue(),
                    teacherB.getId().intValue()
                )));

            // Document
            actions.andDo(document("teacher-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listTeachersWithPaging() throws Exception {
            // Given
            createAndSaveTeacher();
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    teacherB.getId().intValue(),
                    teacherA.getId().intValue()
                )));
        }

        @Test
        void searchTeachersByAcademyId() throws Exception {
            // Given
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();
            createAndSaveTeacher();

            Academy academy = createAndSaveAcademy();
            teacherA.applyAcademy(academy);
            teacherB.applyAcademy(academy);
            academy.acceptTeacher(teacherA);
            academy.acceptTeacher(teacherB);

            academyRepository.save(academy);

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("academyId", academy.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherA.getId().intValue(),
                    teacherB.getId().intValue()
                )));

            // Document
            actions.andDo(document("teacher-search-example",
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
        void searchTeachersByAppliedAcademyId() throws Exception {
            // Given
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();
            createAndSaveTeacher();

            Academy academy = createAndSaveAcademy();
            teacherA.applyAcademy(academy);
            teacherB.applyAcademy(academy);

            teacherRepository.save(teacherA);
            teacherRepository.save(teacherB);

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("appliedAcademyId", academy.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherA.getId().intValue(),
                    teacherB.getId().intValue()
                )));
        }

        @Test
        void searchTeachersByClassroomId() throws Exception {
            // Given
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();
            createAndSaveTeacher();

            Classroom classroom = createAndSaveClassroom();
            teacherA.applyClassroom(classroom);
            teacherB.applyClassroom(classroom);
            classroom.acceptTeacher(teacherA);
            classroom.acceptTeacher(teacherB);

            classroomRepository.save(classroom);

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("classroomId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherA.getId().intValue(),
                    teacherB.getId().intValue()
                )));
        }

        @Test
        void searchTeachersByAppliedClassroomId() throws Exception {
            // Given
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();
            createAndSaveTeacher();

            Classroom classroom = createAndSaveClassroom();
            teacherA.applyClassroom(classroom);
            teacherB.applyClassroom(classroom);

            teacherRepository.save(teacherA);
            teacherRepository.save(teacherB);

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("appliedClassroomId", classroom.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherA.getId().intValue(),
                    teacherB.getId().intValue()
                )));
        }

    }

    @Test
    void postTeacher() throws Exception {
        // When
        TeacherDto.Create dto = TeacherDto.Create.builder()
            .account(AccountDto.Create.builder()
                .username("testusername")
                .password("testpassword")
                .fullName("Test FullName")
                .phone("01012345678")
                .build())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/teachers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.account.id").doesNotExist())
            .andExpect(jsonPath("$.account.username").value("testusername"))
            .andExpect(jsonPath("$.account.password").doesNotExist())
            .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
            .andExpect(jsonPath("$.account.phone").value("01012345678"));

        // Document
        actions.andDo(document("teacher-create-example",
            requestFields(
                DOC_FIELD_ACCOUNT_USERNAME,
                DOC_FIELD_ACCOUNT_PASSWORD,
                DOC_FIELD_ACCOUNT_FULL_NAME,
                DOC_FIELD_ACCOUNT_PHONE
            )));
    }

    @Nested
    class PatchTeacher {

        @Test
        void patchTeacher() throws Exception {
            // Given
            Teacher teacher = teacherRepository.save(new Teacher());
            Account account = accountRepository.save(Account.builder()
                .username("testusername")
                .password("beforePwd")
                .fullName("Before FullName")
                .phone("01011112222")
                .build());
            teacher.setAccount(account);
            teacherRepository.save(teacher);

            // When
            TeacherDto.Update dto = TeacherDto.Update.builder()
                .account(AccountDto.Update.builder()
                    .password("testpassword")
                    .fullName("Test FullName")
                    .phone("01012345678")
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/teachers/{id}", teacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teacher.getId()))
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("testusername"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));

            // Document
            actions.andDo(document("teacher-update-example",
                requestFields(
                    DOC_FIELD_ACCOUNT_PASSWORD,
                    DOC_FIELD_ACCOUNT_FULL_NAME,
                    DOC_FIELD_ACCOUNT_PHONE
                )));
        }

        @Test
        void patchTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aTeacherUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/teachers/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteTeacher {

        @Test
        void deleteTeacher() throws Exception {
            // Given
            Long teacherId = createAndSaveTeacher().getId();

            Long academyAId = createAndSaveAcademy().getId();
            addAcademy(teacherId, academyAId);

            Long academyBId = createAndSaveAcademy().getId();
            addAppliedAcademy(teacherId, academyBId);

            Long classroomAId = createAndSaveClassroom().getId();
            addClassroom(teacherId, classroomAId);

            Long classroomBId = createAndSaveClassroom().getId();
            addAppliedClassroom(teacherId, classroomBId);

            // When
            ResultActions actions = mockMvc.perform(delete("/teachers/{id}", teacherId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(teacherRepository.findById(teacherId)).isEmpty();

            // Document
            actions.andDo(document("teacher-delete-example"));
        }

        @Test
        void deleteTeacher_TeacherNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/teachers/{id}", 0L));

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
            Teacher teacher = createAndSaveTeacher();
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(aTeacherApplyAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("teacher-apply-academy-example",
                requestFields(
                    fieldWithPath("academyId").description("가입 요청할 학원 ID")
                )));
        }

        @Test
        void applyAcademy_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aTeacherApplyAcademyDto());

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(aTeacherApplyAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyAcademy_TeacherAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();
            Academy academy = createAndSaveAcademy();

            teacher.applyAcademy(academy);
            academy.acceptTeacher(teacher);
            academyRepository.save(academy);

            // When
            String body = toJson(aTeacherApplyAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacher.getId())
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
            Teacher teacher = createAndSaveTeacher();
            Academy academy = createAndSaveAcademy();

            teacher.applyAcademy(academy);
            teacherRepository.save(teacher);

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("teacher-remove-applied-academy-example",
                requestFields(
                    fieldWithPath("academyId").description("가입 요청을 철회할 학원 ID")
                )));
        }

        @Test
        void removeAppliedAcademy_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto());

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedAcademy_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacher.getId())
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
            Teacher teacher = createAndSaveTeacher();
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(aTeacherApplyClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("teacher-apply-classroom-example",
                requestFields(
                    fieldWithPath("classroomId").description("가입 요청할 반 ID")
                )));
        }

        @Test
        void applyClassroom_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aTeacherApplyClassroomDto());

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(aTeacherApplyClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyClassroom_TeacherAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();
            Classroom classroom = createAndSaveClassroom();

            teacher.applyClassroom(classroom);
            classroom.acceptTeacher(teacher);
            classroomRepository.save(classroom);

            // When
            String body = toJson(aTeacherApplyClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacher.getId())
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
            Teacher teacher = createAndSaveTeacher();
            Classroom classroom = createAndSaveClassroom();

            teacher.applyClassroom(classroom);
            teacherRepository.save(teacher);

            // When
            String body = toJson(
                aTeacherRemoveAppliedClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("teacher-remove-applied-classroom-example",
                requestFields(
                    fieldWithPath("classroomId").description("가입 요청을 철회할 반 ID")
                )));
        }

        @Test
        void removeAppliedClassroom_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(aTeacherRemoveAppliedClassroomDto());

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedClassroom_ClassroomNotFound_NotFoundStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(aTeacherRemoveAppliedClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedClassroom_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();
            Classroom classroom = createAndSaveClassroom();

            // When
            String body = toJson(
                aTeacherRemoveAppliedClassroomDto().withClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacher.getId())
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

    private Teacher createAndSaveTeacher() {
        Teacher teacher = teacherRepository.save(new Teacher());
        Account account = accountRepository.save(Account.builder()
            .username("testusername")
            .password("testpassword")
            .fullName("Test FullName")
            .phone("01012345678")
            .build());
        teacher.setAccount(account);
        teacherRepository.save(teacher);
        return teacher;
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

    private void addAcademy(Long teacherId, Long academyId) {
        withTransaction(status -> {
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();
            Academy academy = academyRepository.findById(academyId).orElseThrow();

            academy.addTeacher(teacher);
        });
    }

    private void addAppliedAcademy(Long teacherId, Long academyId) {
        withTransaction(status -> {
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();
            Academy academy = academyRepository.findById(academyId).orElseThrow();

            teacher.applyAcademy(academy);
        });
    }

    private void addClassroom(Long teacherId, Long classroomId) {
        withTransaction(status -> {
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();

            classroom.addTeacher(teacher);
        });
    }

    private void addAppliedClassroom(Long teacherId, Long classroomId) {
        withTransaction(status -> {
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();
            Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();

            teacher.applyClassroom(classroom);
        });
    }

}
