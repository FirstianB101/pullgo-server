package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TeacherIntegrationTest {

    @Autowired
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

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
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
                .andExpect(jsonPath("$.account.username").value("testusername"))
                .andExpect(jsonPath("$.account.password").value("testpassword"))
                .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));
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
            .andExpect(jsonPath("$.account.username").value("testusername"))
            .andExpect(jsonPath("$.account.password").value("testpassword"))
            .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
            .andExpect(jsonPath("$.account.phone").value("01012345678"));
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
                .andExpect(jsonPath("$.account.username").value("testusername"))
                .andExpect(jsonPath("$.account.password").value("testpassword"))
                .andExpect(jsonPath("$.account.fullName").value("Test FullName"))
                .andExpect(jsonPath("$.account.phone").value("01012345678"));
        }

        @Test
        void patchTeacher_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(teacherUpdateDto());

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
            Teacher teacher = createAndSaveTeacher();

            // When
            ResultActions actions = mockMvc.perform(delete("/teachers/{id}", teacher.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(teacherRepository.findById(teacher.getId())).isEmpty();
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
            String body = toJson(applyAcademyDtoWithAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-academy", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void applyAcademy_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(applyAcademyDto());

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
            String body = toJson(applyAcademyDtoWithAcademyId(0L));

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
            String body = toJson(applyAcademyDtoWithAcademyId(academy.getId()));

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
            String body = toJson(removeAppliedAcademyDtoWithAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-academy", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void removeAppliedAcademy_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(removeAppliedAcademyDto());

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
            String body = toJson(removeAppliedAcademyDtoWithAcademyId(0L));

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
            String body = toJson(removeAppliedAcademyDtoWithAcademyId(academy.getId()));

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
            String body = toJson(applyClassroomDtoWithClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/apply-classroom", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void applyClassroom_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(applyClassroomDto());

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
            String body = toJson(applyClassroomDtoWithClassroomId(0L));

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
            String body = toJson(applyClassroomDtoWithClassroomId(classroom.getId()));

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
            String body = toJson(removeAppliedClassroomDtoWithClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/teachers/{id}/remove-applied-classroom", teacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void removeAppliedClassroom_TeacherNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(removeAppliedClassroomDto());

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
            String body = toJson(removeAppliedClassroomDtoWithClassroomId(0L));

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
            String body = toJson(removeAppliedClassroomDtoWithClassroomId(classroom.getId()));

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

    private TeacherDto.Update teacherUpdateDto() {
        return TeacherDto.Update.builder()
            .account(AccountDto.Update.builder()
                .password("testpassword")
                .fullName("Test FullName")
                .phone("01012345678")
                .build())
            .build();
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

    private TeacherDto.ApplyAcademy applyAcademyDto() {
        return TeacherDto.ApplyAcademy.builder()
            .academyId(0L)
            .build();
    }

    private TeacherDto.ApplyAcademy applyAcademyDtoWithAcademyId(Long id) {
        return TeacherDto.ApplyAcademy.builder()
            .academyId(id)
            .build();
    }

    private TeacherDto.RemoveAppliedAcademy removeAppliedAcademyDto() {
        return TeacherDto.RemoveAppliedAcademy.builder()
            .academyId(0L)
            .build();
    }

    private TeacherDto.RemoveAppliedAcademy removeAppliedAcademyDtoWithAcademyId(Long id) {
        return TeacherDto.RemoveAppliedAcademy.builder()
            .academyId(id)
            .build();
    }

    private TeacherDto.ApplyClassroom applyClassroomDto() {
        return TeacherDto.ApplyClassroom.builder()
            .classroomId(0L)
            .build();
    }

    private TeacherDto.ApplyClassroom applyClassroomDtoWithClassroomId(Long id) {
        return TeacherDto.ApplyClassroom.builder()
            .classroomId(id)
            .build();
    }

    private TeacherDto.RemoveAppliedClassroom removeAppliedClassroomDto() {
        return TeacherDto.RemoveAppliedClassroom.builder()
            .classroomId(0L)
            .build();
    }

    private TeacherDto.RemoveAppliedClassroom removeAppliedClassroomDtoWithClassroomId(Long id) {
        return TeacherDto.RemoveAppliedClassroom.builder()
            .classroomId(id)
            .build();
    }

}
