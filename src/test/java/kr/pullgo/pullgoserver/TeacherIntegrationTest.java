package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.AccountHelper.*;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherApplyAcademyDto;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherApplyClassroomDto;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacherCreateDto;
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
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.docs.ApiDocumentation;
import kr.pullgo.pullgoserver.dto.AccountDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.dto.mapper.TeacherDtoMapper;
import kr.pullgo.pullgoserver.helper.AccountHelper;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.model.UserRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
    private static final FieldDescriptor DOC_FIELD_ACCOUNT_ROLE =
        fieldWithPath("account.role").description("시스템 역할 (`USER`, `ADMIN`)");
    private static final FieldDescriptor DOC_FIELD_USERNAME_DUPLICATION =
        fieldWithPath("isExists").description("사용자 ID 중복 여부");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherRepository teacherRepository;

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
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class PostTeacher {

        @Test
        void postTeacher(@Autowired PasswordEncoder passwordEncoder) throws Exception {
            // When
            TeacherDto.Create dto = TeacherDto.Create.builder()
                .account(AccountDto.Create.builder()
                    .username("pte1024")
                    .password("this!sPassw0rd")
                    .fullName("박태언")
                    .phone("01012345678")
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(post("/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("pte1024"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("박태언"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"))
                .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            TeacherDto.Result resultDto = fromJson(responseBody, TeacherDto.Result.class);

            String encodedPassword = trxHelper.doInTransaction(() -> {
                Teacher teacher = teacherRepository.findById(resultDto.getId()).orElseThrow();
                return teacher.getAccount().getPassword();
            });
            assertThat(passwordEncoder.matches("this!sPassw0rd", encodedPassword)).isTrue();

            // Document
            actions.andDo(document("teacher-create-example",
                requestFields(
                    DOC_FIELD_ACCOUNT_USERNAME,
                    DOC_FIELD_ACCOUNT_PASSWORD,
                    DOC_FIELD_ACCOUNT_FULL_NAME,
                    DOC_FIELD_ACCOUNT_PHONE
                )));
        }

        @Test
        void postTeacher_TeacherAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            String username = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                return teacher.getAccount().getUsername();
            });

            TeacherDto.Create dto = aTeacherCreateDto().withAccount(
                anAccountCreateDto().withUsername(username)
            );
            String body = toJson(dto);

            // When
            ResultActions actions = mockMvc.perform(post("/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetTeacher {

        @Test
        void getTeacher() throws Exception {
            // Given
            Long teacherId = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withUsername("pte1024")
                        .withFullName("박태언")
                        .withPhone("01012345678")
                        .withRole(UserRole.USER)
                );
                Teacher teacher = entityHelper.generateTeacher(it ->
                    it.withAccount(account)
                );
                return teacher.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(get("/teachers/{id}", teacherId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teacherId))
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("pte1024"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("박태언"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));

            // Document
            actions.andDo(document("teacher-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_ACCOUNT_USERNAME,
                    DOC_FIELD_ACCOUNT_FULL_NAME,
                    DOC_FIELD_ACCOUNT_PHONE,
                    DOC_FIELD_ACCOUNT_ROLE
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
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacherA = entityHelper.generateTeacher();
                Teacher teacherB = entityHelper.generateTeacher();

                return new Struct()
                    .withValue("teacherAId", teacherA.getId())
                    .withValue("teacherBId", teacherB.getId());
            });
            Long teacherAId = given.valueOf("teacherAId");
            Long teacherBId = given.valueOf("teacherBId");

            // When
            ResultActions actions = mockMvc.perform(get("/teachers"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherAId.intValue(),
                    teacherBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateTeacher();
                Teacher teacherA = entityHelper.generateTeacher();
                Teacher teacherB = entityHelper.generateTeacher();

                return new Struct()
                    .withValue("teacherAId", teacherA.getId())
                    .withValue("teacherBId", teacherB.getId());
            });
            Long teacherAId = given.valueOf("teacherAId");
            Long teacherBId = given.valueOf("teacherBId");

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
                    teacherBId.intValue(),
                    teacherAId.intValue()
                )));
        }

        @Test
        void searchTeachersByAcademyId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();

                Teacher teacherA = entityHelper.generateTeacher(it -> {
                    Teacher oldOwner = academy.getOwner();

                    academy.addTeacher(it);
                    academy.setOwner(it);
                    academy.removeTeacher(oldOwner);
                    return it;
                });
                Teacher teacherB = entityHelper.generateTeacher(it -> {
                    academy.addTeacher(it);
                    return it;
                });
                entityHelper.generateTeacher();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("teacherAId", teacherA.getId())
                    .withValue("teacherBId", teacherB.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherAId = given.valueOf("teacherAId");
            Long teacherBId = given.valueOf("teacherBId");

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("academyId", academyId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherAId.intValue(),
                    teacherBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();

                Teacher teacherA = entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                Teacher teacherB = entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                entityHelper.generateTeacher();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("teacherAId", teacherA.getId())
                    .withValue("teacherBId", teacherB.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherAId = given.valueOf("teacherAId");
            Long teacherBId = given.valueOf("teacherBId");

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("appliedAcademyId", academyId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherAId.intValue(),
                    teacherBId.intValue()
                )));
        }

        @Test
        void searchTeachersByClassroomId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Teacher teacherA = entityHelper.generateTeacher(it -> {
                    classroom.addTeacher(it);
                    return it;
                });
                Teacher teacherB = entityHelper.generateTeacher(it -> {
                    classroom.addTeacher(it);
                    return it;
                });
                entityHelper.generateTeacher();

                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherAId", teacherA.getId())
                    .withValue("teacherBId", teacherB.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherAId = given.valueOf("teacherAId");
            Long teacherBId = given.valueOf("teacherBId");

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("classroomId", classroomId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherAId.intValue(),
                    teacherBId.intValue()
                )));
        }

        @Test
        void searchTeachersByAppliedClassroomId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                Teacher teacherA = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                Teacher teacherB = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                entityHelper.generateTeacher();

                return new Struct()
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherAId", teacherA.getId())
                    .withValue("teacherBId", teacherB.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherAId = given.valueOf("teacherAId");
            Long teacherBId = given.valueOf("teacherBId");

            // When
            ResultActions actions = mockMvc.perform(get("/teachers")
                .param("appliedClassroomId", classroomId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    teacherAId.intValue(),
                    teacherBId.intValue()
                )));
        }

    }

    @Nested
    class PatchTeacher {

        @Test
        void patchTeacher(@Autowired PasswordEncoder passwordEncoder) throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withUsername("pte1024")
                        .withPassword("oldPassw0rd")
                        .withFullName("박언태")
                        .withPhone("01011112222")
                );
                Teacher teacher = entityHelper.generateTeacher(it ->
                    it.withAccount(account)
                );
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            String token = given.valueOf("token");
            Long teacherId = given.valueOf("teacherId");

            // When
            TeacherDto.Update dto = TeacherDto.Update.builder()
                .account(AccountDto.Update.builder()
                    .password("newPassw0rd")
                    .fullName("박태언")
                    .phone("01012345678")
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/teachers/{id}", teacherId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teacherId))
                .andExpect(jsonPath("$.account.id").doesNotExist())
                .andExpect(jsonPath("$.account.username").value("pte1024"))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.account.fullName").value("박태언"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"))
                .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            TeacherDto.Result resultDto = fromJson(responseBody, TeacherDto.Result.class);

            String encodedPassword = trxHelper.doInTransaction(() -> {
                Teacher teacher = teacherRepository.findById(resultDto.getId()).orElseThrow();
                return teacher.getAccount().getPassword();
            });
            assertThat(passwordEncoder.matches("newPassw0rd", encodedPassword)).isTrue();

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
            Struct given = trxHelper.doInTransaction(() -> {
                Academy joiningAcademy = entityHelper.generateAcademy();
                Academy applyingAcademy = entityHelper.generateAcademy();
                Classroom joiningClassroom = entityHelper.generateClassroom();
                Classroom applyingClassroom = entityHelper.generateClassroom();

                Teacher teacher = entityHelper.generateTeacher(it -> {
                    joiningAcademy.addTeacher(it);
                    joiningClassroom.addTeacher(it);
                    it.applyAcademy(applyingAcademy);
                    it.applyClassroom(applyingClassroom);
                    return it;
                });
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            String token = given.valueOf("token");
            Long teacherId = given.valueOf("teacherId");

            // When
            ResultActions actions = mockMvc.perform(delete("/teachers/{id}", teacherId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(teacherRepository.findById(teacherId)).isEmpty();

            // Document
            actions.andDo(document("teacher-delete-example"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
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
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherApplyAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");


            // When
            String body = toJson(aTeacherApplyAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyAcademy_TeacherAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    academy.addTeacher(it);
                    return it;
                });
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherApplyAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class RemoveAppliedAcademy {

        @Test
        void 학원_가입요청_철회() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId())
                    .withValue("token", token);
            });
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
        void 학원_가입요청_거절() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                Teacher creator = academy.getOwner();
                String token = entityHelper.generateToken(it -> creator.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId());
            });
            String token = given.valueOf("token");
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("teacher-remove-applied-academy-example",
                requestFields(
                    fieldWithPath("academyId").description("가입 요청을 거절할 학원 ID")
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
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedAcademy_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher();
                Teacher creator = academy.getOwner();
                String token = entityHelper.generateToken(it -> creator.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherRemoveAppliedAcademyDto().withAcademyId(academyId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherApplyClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherApplyClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void applyClassroom_TeacherAlreadyEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    classroom.addTeacher(it);
                    return it;
                });
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherApplyClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class RemoveAppliedClassroom {

        @Test
        void 반_가입요청_철회() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });

                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(
                aTeacherRemoveAppliedClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
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
        void 반_가입요청_거절() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher classroomTeacher = entityHelper.generateTeacher();
                Classroom classroom = entityHelper.generateClassroom(it ->{
                    it.addTeacher(classroomTeacher);
                    return it;
                });
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                String token = entityHelper.generateToken(it -> classroomTeacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(
                aTeacherRemoveAppliedClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("teacher-remove-applied-classroom-example",
                requestFields(
                    fieldWithPath("classroomId").description("가입 요청을 거절할 반 ID")
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
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(aTeacherRemoveAppliedClassroomDto().withClassroomId(0L));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void removeAppliedClassroom_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher classroomTeacher = entityHelper.generateTeacher();
                Classroom classroom = entityHelper.generateClassroom(it ->{
                    it.addTeacher(classroomTeacher);
                    return it;
                });
                Teacher teacher = entityHelper.generateTeacher();
                String token = entityHelper.generateToken(it -> classroomTeacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("classroomId", classroom.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long classroomId = given.valueOf("classroomId");
            Long teacherId = given.valueOf("teacherId");
            String token = given.valueOf("token");

            // When
            String body = toJson(
                aTeacherRemoveAppliedClassroomDto().withClassroomId(classroomId));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacherId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class CheckDuplicateUsername {

        @Test
        public void 중복되지_않는_username_중복확인조회() throws Exception {
            //When
            ResultActions actions = mockMvc.perform(get("/teachers/{username}/exists", "newUser"));

            //Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isExists").value(false));

            // Document
            actions.andDo(document("teacher-check-duplicate-username-example",
                responseFields(
                    DOC_FIELD_USERNAME_DUPLICATION
                )));
        }

        @Test
        public void 중복되는_username_중복확인조회() throws Exception {
            // Given
            String username = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                return teacher.getAccount().getUsername();
            });

            // When
            ResultActions actions = mockMvc.perform(get("/teachers/{username}/exists",
                username));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isExists").value(true));
        }
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String responseBody, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, clazz);
    }

}
