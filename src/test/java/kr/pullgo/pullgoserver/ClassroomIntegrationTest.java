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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ClassroomIntegrationTest {

    @Autowired
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

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
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

    @Test
    void postClassroom() throws Exception {
        // Given
        Academy academy = createAndSaveAcademy();

        // When
        ClassroomDto.Create dto = ClassroomDto.Create.builder()
            .name("test name")
            .academyId(academy.getId())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/academy/classrooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("test name"))
            .andExpect(jsonPath("$.academyId").value(academy.getId()));
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
            Classroom classroom = createAndSaveClassroom();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academy/classrooms/{id}", classroom.getId()));

            // Then
            actions
                .andExpect(status().isNoContent());

            assertThat(classroomRepository.findById(classroom.getId())).isEmpty();
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
