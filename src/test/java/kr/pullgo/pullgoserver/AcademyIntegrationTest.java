package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyCreateDtoWithOwnerId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyUpdateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyUpdateDtoWithOwnerId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptTeacherDtoWithTeacherId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickTeacherDtoWithTeacherId;
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
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
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
public class AcademyIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("학원 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("학원 이름");
    private static final FieldDescriptor DOC_FIELD_PHONE =
        fieldWithPath("phone").description("학원 전화번호");
    private static final FieldDescriptor DOC_FIELD_ADDRESS =
        fieldWithPath("address").description("학원 주소");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID =
        fieldWithPath("ownerId").description("학원을 소유한 선생님 ID");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class GetAcademy {

        @Test
        void getAcademy() throws Exception {
            // Given
            Teacher owner = createAndSaveTeacher();

            Academy academy = Academy.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .build();
            academy.addTeacher(owner);
            academy.setOwner(owner);
            academy = academyRepository.save(academy);

            // When
            ResultActions actions = mockMvc.perform(get("/academies/{id}", academy.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(academy.getId()))
                .andExpect(jsonPath("$.name").value("Test academy"))
                .andExpect(jsonPath("$.phone").value("01012345678"))
                .andExpect(jsonPath("$.address").value("Seoul"))
                .andExpect(jsonPath("$.ownerId").value(owner.getId()));

            // Document
            actions.andDo(document("academy-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_PHONE,
                    DOC_FIELD_ADDRESS,
                    DOC_FIELD_OWNER_ID
                )));
        }

        @Test
        void getAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/academies/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchAcademies {

        @Test
        void listAcademies() throws Exception {
            // Given
            Academy academyA = createAndSaveAcademy();
            Academy academyB = createAndSaveAcademy();

            // When
            ResultActions actions = mockMvc.perform(get("/academies"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyA.getId().intValue(),
                    academyB.getId().intValue()
                )));

            // Document
            actions.andDo(document("academy-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listAcademiesWithPaging() throws Exception {
            // Given
            createAndSaveAcademy();
            Academy academyA = createAndSaveAcademy();
            Academy academyB = createAndSaveAcademy();

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    academyB.getId().intValue(),
                    academyA.getId().intValue()
                )));

            // Document
            actions.andDo(document("academy-list-with-paging-example"));
        }

        @Test
        void searchAcademiesByOwnerId() throws Exception {
            // Given
            Teacher teacher = createAndSaveTeacher();

            Academy academyA = createAndSaveAcademyWithOwner(teacher);
            Academy academyB = createAndSaveAcademyWithOwner(teacher);
            createAndSaveAcademy();

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("ownerId", teacher.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyA.getId().intValue(),
                    academyB.getId().intValue()
                )));

            // Document
            actions.andDo(document("academy-search-example",
                requestParameters(
                    parameterWithName("ownerId")
                        .description("학원 소유자 ID").optional(),
                    parameterWithName("studentId")
                        .description("학원에 등록된 학생 ID").optional(),
                    parameterWithName("applyingStudentId")
                        .description("학원에 가입 요청한 학생 ID").optional(),
                    parameterWithName("teacherId")
                        .description("학원에 등록된 선생님 ID").optional(),
                    parameterWithName("applyingTeacherId")
                        .description("학원에 가입 요청한 선생님 ID").optional()
                )));
        }

        @Test
        void searchAcademiesByStudentId() throws Exception {
            // Given
            Academy academyA = createAndSaveAcademy();
            Academy academyB = createAndSaveAcademy();
            createAndSaveAcademy();

            Student student = createAndSaveStudent();

            student.applyAcademy(academyA);
            student.applyAcademy(academyB);

            academyA.acceptStudent(student);
            academyB.acceptStudent(student);

            academyRepository.save(academyA);
            academyRepository.save(academyB);

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("studentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyA.getId().intValue(),
                    academyB.getId().intValue()
                )));
        }

        @Test
        void searchAcademiesByApplyingStudentId() throws Exception {
            // Given
            Academy academyA = createAndSaveAcademy();
            Academy academyB = createAndSaveAcademy();
            createAndSaveAcademy();

            Student student = createAndSaveStudent();

            student.applyAcademy(academyA);
            student.applyAcademy(academyB);

            studentRepository.save(student);

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("applyingStudentId", student.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyA.getId().intValue(),
                    academyB.getId().intValue()
                )));
        }

        @Test
        void searchAcademiesByTeacherId() throws Exception {
            // Given
            Academy academyA = createAndSaveAcademy();
            Academy academyB = createAndSaveAcademy();
            createAndSaveAcademy();

            Teacher teacher = createAndSaveTeacher();

            teacher.applyAcademy(academyA);
            teacher.applyAcademy(academyB);

            academyA.acceptTeacher(teacher);
            academyB.acceptTeacher(teacher);

            academyRepository.save(academyA);
            academyRepository.save(academyB);

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("teacherId", teacher.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyA.getId().intValue(),
                    academyB.getId().intValue()
                )));
        }

        @Test
        void searchAcademiesByApplyingTeacherId() throws Exception {
            // Given
            Academy academyA = createAndSaveAcademy();
            Academy academyB = createAndSaveAcademy();
            createAndSaveAcademy();

            Teacher teacher = createAndSaveTeacher();

            teacher.applyAcademy(academyA);
            teacher.applyAcademy(academyB);

            teacherRepository.save(teacher);

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("applyingTeacherId", teacher.getId().toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyA.getId().intValue(),
                    academyB.getId().intValue()
                )));
        }

    }

    @Nested
    class PostAcademy {

        @Test
        void postAcademy() throws Exception {
            // Given
            Teacher creator = createAndSaveTeacher();

            // When
            AcademyDto.Create dto = AcademyDto.Create.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .ownerId(creator.getId())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(post("/academies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test academy"))
                .andExpect(jsonPath("$.phone").value("01012345678"))
                .andExpect(jsonPath("$.address").value("Seoul"))
                .andExpect(jsonPath("$.ownerId").value(creator.getId()));

            // Document
            actions.andDo(document("academy-create-example",
                requestFields(
                    DOC_FIELD_NAME,
                    DOC_FIELD_PHONE,
                    DOC_FIELD_ADDRESS,
                    DOC_FIELD_OWNER_ID
                )));
        }

        @Test
        void postAcademy_InvalidOwner_NotFoundStatus() throws Exception {
            // When
            AcademyDto.Create dto = academyCreateDtoWithOwnerId(0L);
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(post("/academies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class PatchAcademy {

        @Test
        void patchAcademy() throws Exception {
            // Given
            Teacher teacherA = createAndSaveTeacher();
            Teacher teacherB = createAndSaveTeacher();

            Academy academy = Academy.builder()
                .name("Before academy")
                .phone("01011112222")
                .address("Busan")
                .build();

            academy.addTeacher(teacherA);
            academy.addTeacher(teacherB);

            academy.setOwner(teacherA);

            academy = academyRepository.save(academy);

            // When
            AcademyDto.Update dto = AcademyDto.Update.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .ownerId(teacherB.getId())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/academies/{id}", academy.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(academy.getId()))
                .andExpect(jsonPath("$.name").value("Test academy"))
                .andExpect(jsonPath("$.phone").value("01012345678"))
                .andExpect(jsonPath("$.address").value("Seoul"))
                .andExpect(jsonPath("$.ownerId").value(teacherB.getId()));

            // Document
            actions.andDo(document("academy-update-example",
                requestFields(
                    DOC_FIELD_NAME.optional(),
                    DOC_FIELD_PHONE.optional(),
                    DOC_FIELD_ADDRESS.optional(),
                    DOC_FIELD_OWNER_ID.optional()
                )));
        }

        @Test
        void patchAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(academyUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/academies/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void patchAcademy_InvalidOwner_NotFoundStatus() throws Exception {
            // When
            String body = toJson(academyUpdateDtoWithOwnerId(0L));

            ResultActions actions = mockMvc.perform(patch("/academies/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteAcademy {

        @Test
        void deleteAcademy() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();

            // When
            ResultActions actions = mockMvc.perform(delete("/academies/{id}", academy.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(academyRepository.findById(academy.getId())).isEmpty();

            // Document
            actions.andDo(document("academy-delete-example"));
        }

        @Test
        void deleteAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/academies/{id}", 0));

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
            Academy academy = createAndSaveAcademy();
            Teacher teacher = createAndSaveTeacher();

            teacher.applyAcademy(academy);
            teacherRepository.save(teacher);

            // When
            String body = toJson(acceptTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("academy-accept-teacher-example",
                requestFields(
                    fieldWithPath("teacherId").description("승인할 선생님 ID")
                )));
        }

        @Test
        void acceptTeacher_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(acceptTeacherDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(acceptTeacherDtoWithTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptTeacher_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(acceptTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", academy.getId())
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
            Academy academy = createAndSaveAcademy();
            Teacher teacher = createAndSaveTeacher();

            teacher.applyAcademy(academy);
            academy.acceptTeacher(teacher);
            academyRepository.save(academy);

            // When
            String body = toJson(kickTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("academy-kick-teacher-example",
                requestFields(
                    fieldWithPath("teacherId").description("추방할 선생님 ID")
                )));
        }

        @Test
        void kickTeacher_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(kickTeacherDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(kickTeacherDtoWithTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickTeacher_TeacherNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();
            Teacher teacher = createAndSaveTeacher();

            // When
            String body = toJson(kickTeacherDtoWithTeacherId(teacher.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", academy.getId())
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
            Academy academy = createAndSaveAcademy();
            Student student = createAndSaveStudent();

            student.applyAcademy(academy);
            studentRepository.save(student);

            // When
            String body = toJson(acceptStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("academy-accept-student-example",
                requestFields(
                    fieldWithPath("studentId").description("승인할 학생 ID")
                )));
        }

        @Test
        void acceptStudent_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(acceptStudentDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(acceptStudentDtoWithStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptStudent_StudentNotApplied_BadRequestStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();
            Student student = createAndSaveStudent();

            // When
            String body = toJson(acceptStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", academy.getId())
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
            Academy academy = createAndSaveAcademy();
            Student student = createAndSaveStudent();

            student.applyAcademy(academy);
            academy.acceptStudent(student);
            academyRepository.save(academy);

            // When
            String body = toJson(kickStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            // Document
            actions.andDo(document("academy-kick-student-example",
                requestFields(
                    fieldWithPath("studentId").description("추방할 학생 ID")
                )));
        }

        @Test
        void kickStudent_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(kickStudentDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();

            // When
            String body = toJson(kickStudentDtoWithStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", academy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void kickStudent_StudentNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Academy academy = createAndSaveAcademy();
            Student student = createAndSaveStudent();

            // When
            String body = toJson(kickStudentDtoWithStudentId(student.getId()));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", academy.getId())
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

    private Academy createAndSaveAcademyWithOwner(Teacher owner) {
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .build();
        academy.addTeacher(owner);
        academy.setOwner(owner);
        return academyRepository.save(academy);
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
