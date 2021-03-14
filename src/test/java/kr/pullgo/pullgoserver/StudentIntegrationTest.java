package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class StudentIntegrationTest {

    @Autowired
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

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
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
        }

        @Test
        void patchStudent_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(studentUpdateDto());

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
            Student student = createAndSaveStudent();

            // When
            ResultActions actions = mockMvc.perform(delete("/students/{id}", student.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(studentRepository.findById(student.getId())).isEmpty();
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
            String body = toJson(applyAcademyDtoWithAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-academy", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void applyAcademy_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(applyAcademyDto());

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
            String body = toJson(applyAcademyDtoWithAcademyId(0L));

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
            String body = toJson(applyAcademyDtoWithAcademyId(academy.getId()));

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
            String body = toJson(removeAppliedAcademyDtoWithAcademyId(academy.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-academy", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void removeAppliedAcademy_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(removeAppliedAcademyDto());

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
            String body = toJson(removeAppliedAcademyDtoWithAcademyId(0L));

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
            String body = toJson(removeAppliedAcademyDtoWithAcademyId(academy.getId()));

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
            String body = toJson(applyClassroomDtoWithClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/apply-classroom", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void applyClassroom_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(applyClassroomDto());

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
            String body = toJson(applyClassroomDtoWithClassroomId(0L));

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
            String body = toJson(applyClassroomDtoWithClassroomId(classroom.getId()));

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
            String body = toJson(removeAppliedClassroomDtoWithClassroomId(classroom.getId()));

            ResultActions actions = mockMvc
                .perform(post("/students/{id}/remove-applied-classroom", student.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        }

        @Test
        void removeAppliedClassroom_StudentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(removeAppliedClassroomDto());

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
            String body = toJson(removeAppliedClassroomDtoWithClassroomId(0L));

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
            String body = toJson(removeAppliedClassroomDtoWithClassroomId(classroom.getId()));

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

    private StudentDto.Update studentUpdateDto() {
        return StudentDto.Update.builder()
            .parentPhone("01098765432")
            .schoolName("test school")
            .schoolYear(1)
            .account(AccountDto.Update.builder()
                .password("testpassword")
                .fullName("Test FullName")
                .phone("01012345678")
                .build())
            .build();
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

    private StudentDto.ApplyAcademy applyAcademyDto() {
        return StudentDto.ApplyAcademy.builder()
            .academyId(0L)
            .build();
    }

    private StudentDto.ApplyAcademy applyAcademyDtoWithAcademyId(Long id) {
        return StudentDto.ApplyAcademy.builder()
            .academyId(id)
            .build();
    }

    private StudentDto.RemoveAppliedAcademy removeAppliedAcademyDto() {
        return StudentDto.RemoveAppliedAcademy.builder()
            .academyId(0L)
            .build();
    }

    private StudentDto.RemoveAppliedAcademy removeAppliedAcademyDtoWithAcademyId(Long id) {
        return StudentDto.RemoveAppliedAcademy.builder()
            .academyId(id)
            .build();
    }

    private StudentDto.ApplyClassroom applyClassroomDto() {
        return StudentDto.ApplyClassroom.builder()
            .classroomId(0L)
            .build();
    }

    private StudentDto.ApplyClassroom applyClassroomDtoWithClassroomId(Long id) {
        return StudentDto.ApplyClassroom.builder()
            .classroomId(id)
            .build();
    }

    private StudentDto.RemoveAppliedClassroom removeAppliedClassroomDto() {
        return StudentDto.RemoveAppliedClassroom.builder()
            .classroomId(0L)
            .build();
    }

    private StudentDto.RemoveAppliedClassroom removeAppliedClassroomDtoWithClassroomId(Long id) {
        return StudentDto.RemoveAppliedClassroom.builder()
            .classroomId(id)
            .build();
    }

}
