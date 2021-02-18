package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AcademyTest {

    @Autowired
    AcademyRepository academyRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Test
    void acceptStudent() {
        // Given
        Student student = createAndSaveStudent();
        Academy academy = createAndSaveAcademy();

        student.applyAcademy(academy);

        // When
        academy.acceptStudent(student);

        // Then
        assertThat(academy.getStudents())
            .contains(student);
        assertThat(academy.getApplyingStudents())
            .doesNotContain(student);
    }

    @Test
    void acceptStudent_NotApplied_ExceptionThrown() {
        // Given
        Student student = createAndSaveStudent();
        Academy academy = createAndSaveAcademy();

        // When
        Throwable thrown = catchThrowable(() -> academy.acceptStudent(student));

        // Then
        assertThat(thrown).isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void acceptTeacher() {
        // Given
        Teacher teacher = createAndSaveTeacher();
        Academy academy = createAndSaveAcademy();

        teacher.applyAcademy(academy);

        // When
        academy.acceptTeacher(teacher);

        // Then
        assertThat(academy.getTeachers())
            .contains(teacher);
        assertThat(academy.getApplyingTeachers())
            .doesNotContain(teacher);
    }

    @Test
    void acceptTeacher_NotApplied_ExceptionThrown() {
        // Given
        Teacher teacher = createAndSaveTeacher();
        Academy academy = createAndSaveAcademy();

        // When
        Throwable thrown = catchThrowable(() -> academy.acceptTeacher(teacher));

        // Then
        assertThat(thrown).isInstanceOf(TeacherNotFoundException.class);
    }

    private Academy createAndSaveAcademy() {
        return academyRepository.save(
            Academy.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .build()
        );
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