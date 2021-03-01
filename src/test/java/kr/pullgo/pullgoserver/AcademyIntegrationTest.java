package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyUpdateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptTeacherDtoWithTeacherId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickTeacherDtoWithTeacherId;
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
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
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
public class AcademyIntegrationTest {

    @Autowired
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

    @Nested
    class GetAcademy {

        @Test
        void getAcademy() throws Exception {
            // Given
            Academy academy = academyRepository.save(Academy.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .build());

            // When
            ResultActions actions = mockMvc.perform(get("/academies/{id}", academy.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(academy.getId()))
                .andExpect(jsonPath("$.name").value("Test academy"))
                .andExpect(jsonPath("$.phone").value("01012345678"))
                .andExpect(jsonPath("$.address").value("Seoul"));
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

    @Test
    void postAcademy() throws Exception {
        // When
        AcademyDto.Create dto = AcademyDto.Create.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
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
            .andExpect(jsonPath("$.address").value("Seoul"));
    }

    @Nested
    class PatchAcademy {

        @Test
        void patchAcademy() throws Exception {
            // Given
            Academy academy = academyRepository.save(Academy.builder()
                .name("Before academy")
                .phone("01011112222")
                .address("Busan")
                .build());

            // When
            AcademyDto.Update dto = AcademyDto.Update.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
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
                .andExpect(jsonPath("$.address").value("Seoul"));
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
        return academyRepository.save(
            Academy.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .build());
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
