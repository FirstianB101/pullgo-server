package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyAcceptStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyAcceptTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyCreateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyKickStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyKickTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyUpdateDto;
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
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.helper.AuthHelper;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
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
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private AuthHelper authHelper;

    @BeforeEach
    void setUp(
        WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation
    ) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class GetAcademy {

        @Test
        void getAcademy() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy(it ->
                    it.withName("퍼스티안 학원")
                        .withPhone("021234567")
                        .withAddress("서울특별시 노원구 월계1동 광운로 20")
                );

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("ownerId", academy.getOwner().getId());
            });
            Long academyId = given.valueOf("academyId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies/{id}", academyId));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(academyId))
                .andExpect(jsonPath("$.name").value("퍼스티안 학원"))
                .andExpect(jsonPath("$.phone").value("021234567"))
                .andExpect(jsonPath("$.address").value("서울특별시 노원구 월계1동 광운로 20"))
                .andExpect(jsonPath("$.ownerId").value(ownerId));

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
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academyA = entityHelper.generateAcademy();
                Academy academyB = entityHelper.generateAcademy();
                return new Struct()
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
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
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateAcademy();
                Academy academyA = entityHelper.generateAcademy();
                Academy academyB = entityHelper.generateAcademy();

                return new Struct()
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

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
                    academyBId.intValue(),
                    academyAId.intValue()
                )));

            // Document
            actions.andDo(document("academy-list-with-paging-example"));
        }

        @Test
        void searchAcademiesByOwnerId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();

                Academy academyA = entityHelper.generateAcademy(it -> {
                    it.addTeacher(teacher);
                    it.setOwner(teacher);
                    return it;
                });
                Academy academyB = entityHelper.generateAcademy(it -> {
                    it.addTeacher(teacher);
                    it.setOwner(teacher);
                    return it;
                });
                entityHelper.generateAcademy();

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("ownerId", teacherId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
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
                        .description("학원에 가입 요청한 선생님 ID").optional(),
                    parameterWithName("nameLike")
                        .description("유사한 학원 이름").optional()
                )));
        }

        @Test
        void searchAcademiesByStudentId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();

                Academy academyA = entityHelper.generateAcademy(it -> {
                    it.addStudent(student);
                    return it;
                });
                Academy academyB = entityHelper.generateAcademy(it -> {
                    it.addStudent(student);
                    return it;
                });
                entityHelper.generateAcademy();

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("studentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
                )));
        }

        @Test
        void searchAcademiesByApplyingStudentId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academyA = entityHelper.generateAcademy();
                Academy academyB = entityHelper.generateAcademy();
                entityHelper.generateAcademy();

                Student student = entityHelper.generateStudent(it -> {
                    it.applyAcademy(academyA);
                    it.applyAcademy(academyB);
                    return it;
                });

                return new Struct()
                    .withValue("studentId", student.getId())
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("applyingStudentId", studentId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
                )));
        }

        @Test
        void searchAcademiesByTeacherId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();

                Academy academyA = entityHelper.generateAcademy(it -> {
                    it.addTeacher(teacher);
                    return it;
                });
                Academy academyB = entityHelper.generateAcademy(it -> {
                    it.addTeacher(teacher);
                    return it;
                });
                entityHelper.generateAcademy();

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("teacherId", teacherId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
                )));
        }

        @Test
        void searchAcademiesByApplyingTeacherId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academyA = entityHelper.generateAcademy();
                Academy academyB = entityHelper.generateAcademy();
                entityHelper.generateAcademy();

                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academyA);
                    it.applyAcademy(academyB);
                    return it;
                });

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("applyingTeacherId", teacherId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
                )));
        }

        @Test
        void searchAcademiesByNameLike() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academyA = entityHelper.generateAcademy(it -> it.withName("테라스터디"));
                Academy academyB = entityHelper.generateAcademy(it -> it.withName("스터디 에듀"));
                entityHelper.generateAcademy(it -> it.withName("마스터학원"));

                return new Struct()
                    .withValue("academyAId", academyA.getId())
                    .withValue("academyBId", academyB.getId());
            });
            Long academyAId = given.valueOf("academyAId");
            Long academyBId = given.valueOf("academyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/academies")
                .param("nameLike", "스터디"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    academyAId.intValue(),
                    academyBId.intValue()
                )));
        }

    }

    @Nested
    class PostAcademy {

        @Test
        @WithMockUser(authorities = "ADMIN")
        void postAcademy() throws Exception {
            // Given
            Long creatorId = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                return teacher.getId();
            });

            // When
            AcademyDto.Create dto = AcademyDto.Create.builder()
                .name("퍼스티안 학원")
                .phone("021234567")
                .address("서울특별시 노원구 월계1동 광운로 20")
                .ownerId(creatorId)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(post("/academies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("퍼스티안 학원"))
                .andExpect(jsonPath("$.phone").value("021234567"))
                .andExpect(jsonPath("$.address").value("서울특별시 노원구 월계1동 광운로 20"))
                .andExpect(jsonPath("$.ownerId").value(creatorId));

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
        @WithMockUser(authorities = "ADMIN")
        void postAcademy_InvalidOwner_NotFoundStatus() throws Exception {
            // When
            AcademyDto.Create dto = anAcademyCreateDto().withOwnerId(0L);
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
        @WithMockUser(authorities = "ADMIN")
        void patchAcademy() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacherA = entityHelper.generateTeacher();
                Teacher teacherB = entityHelper.generateTeacher();

                Academy academy = entityHelper.generateAcademy(it -> {
                    it = it.withName("퍼스티안 학원")
                        .withPhone("021234567")
                        .withAddress("서울특별시 노원구 월계1동 광운로 20");

                    it.addTeacher(teacherA);
                    it.addTeacher(teacherB);

                    it.setOwner(teacherA);
                    return it;
                });

                return new Struct()
                    .withValue("teacherBId", teacherB.getId())
                    .withValue("academyId", academy.getId());
            });
            Long teacherBId = given.valueOf("teacherBId");
            Long academyId = given.valueOf("academyId");

            // When
            AcademyDto.Update dto = AcademyDto.Update.builder()
                .name("세컨디안 학원")
                .phone("029876543")
                .address("서울특별시 노원구 광운로21 광운대학교 빛솔재(행복기숙사)")
                .ownerId(teacherBId)
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/academies/{id}", academyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(academyId))
                .andExpect(jsonPath("$.name").value("세컨디안 학원"))
                .andExpect(jsonPath("$.phone").value("029876543"))
                .andExpect(jsonPath("$.address").value("서울특별시 노원구 광운로21 광운대학교 빛솔재(행복기숙사)"))
                .andExpect(jsonPath("$.ownerId").value(teacherBId));

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
        @WithMockUser(authorities = "ADMIN")
        void patchAcademy_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAcademyUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/academies/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void patchAcademy_InvalidOwner_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAcademyUpdateDto().withOwnerId(0L));

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
        @WithMockUser(authorities = "ADMIN")
        void deleteAcademy() throws Exception {
            // Given
            Long academyId = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy(it -> {
                    it.addTeacher(entityHelper.generateTeacher());
                    it.addStudent(entityHelper.generateStudent());
                    return it;
                });

                entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academy);
                    return it;
                });
                entityHelper.generateStudent(it -> {
                    it.applyAcademy(academy);
                    return it;
                });

                return academy.getId();
            });

            // When
            ResultActions actions = mockMvc.perform(delete("/academies/{id}", academyId));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(academyRepository.findById(academyId)).isEmpty();

            // Document
            actions.andDo(document("academy-delete-example"));
        }

        @Test
        void 학원산하_반에_대한_가입요청을_받지않고_학원_삭제() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Classroom classroom = entityHelper.generateClassroom();

                entityHelper.generateTeacher(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                entityHelper.generateStudent(it -> {
                    it.applyClassroom(classroom);
                    return it;
                });
                Academy academy = classroom.getAcademy();

                String token = authHelper.generateToken(it -> academy.getOwner().getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("academyId", academy.getId());
            });
            String token = given.valueOf("token");
            Long academyId = given.valueOf("academyId");

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academies/{id}", academyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(academyRepository.findById(academyId)).isEmpty();
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
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
        @WithMockUser(authorities = "ADMIN")
        void acceptTeacher() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher(it -> {
                    it.applyAcademy(academy);
                    return it;
                });

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");

            // When
            String body = toJson(anAcademyAcceptTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void acceptTeacher_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAcademyAcceptTeacherDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void acceptTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // Given
            Long academyId = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                return academy.getId();
            });

            // When
            String body = toJson(anAcademyAcceptTeacherDto().withTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", academyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void acceptTeacher_TeacherNotApplied_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                Teacher teacher = entityHelper.generateTeacher();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("teacherId", teacher.getId());
            });
            Long academyId = given.valueOf("academyId");
            Long teacherId = given.valueOf("teacherId");

            // When
            String body = toJson(anAcademyAcceptTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-teacher", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void kickTeacher() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                Academy academy = entityHelper.generateAcademy(it -> {
                    it.addTeacher(teacher);
                    return it;
                });

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("academyId", academy.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long academyId = given.valueOf("academyId");

            // When
            String body = toJson(anAcademyKickTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void kickTeacher_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAcademyKickTeacherDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void kickTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // Given
            Long academyId = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                return academy.getId();
            });

            // When
            String body = toJson(anAcademyKickTeacherDto().withTeacherId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", academyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void kickTeacher_TeacherNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                Academy academy = entityHelper.generateAcademy();

                return new Struct()
                    .withValue("teacherId", teacher.getId())
                    .withValue("academyId", academy.getId());
            });
            Long teacherId = given.valueOf("teacherId");
            Long academyId = given.valueOf("academyId");

            // When
            String body = toJson(anAcademyKickTeacherDto().withTeacherId(teacherId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-teacher", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void acceptStudent() throws Exception {
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
            String body = toJson(anAcademyAcceptStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void acceptStudent_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAcademyAcceptStudentDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void acceptStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // Given
            Long academyId = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                return academy.getId();
            });

            // When
            String body = toJson(anAcademyAcceptStudentDto().withStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", academyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void acceptStudent_StudentNotApplied_BadRequestStatus() throws Exception {
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
            String body = toJson(anAcademyAcceptStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/accept-student", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void kickStudent() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                Academy academy = entityHelper.generateAcademy(it -> {
                    it.addStudent(student);
                    return it;
                });

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentId", student.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long academyId = given.valueOf("academyId");

            // When
            String body = toJson(anAcademyKickStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", academyId)
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
        @WithMockUser(authorities = "ADMIN")
        void kickStudent_AcademyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAcademyKickStudentDto());

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void kickStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // Given
            Long academyId = trxHelper.doInTransaction(() -> {
                Academy academy = entityHelper.generateAcademy();
                return academy.getId();
            });

            // When
            String body = toJson(anAcademyKickStudentDto().withStudentId(0L));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", academyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void kickStudent_StudentNotEnrolled_BadRequestStatus() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Student student = entityHelper.generateStudent();
                Academy academy = entityHelper.generateAcademy();

                return new Struct()
                    .withValue("academyId", academy.getId())
                    .withValue("studentId", student.getId());
            });
            Long studentId = given.valueOf("studentId");
            Long academyId = given.valueOf("academyId");

            // When
            String body = toJson(anAcademyKickStudentDto().withStudentId(studentId));

            ResultActions actions = mockMvc
                .perform(post("/academies/{id}/kick-student", academyId)
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
