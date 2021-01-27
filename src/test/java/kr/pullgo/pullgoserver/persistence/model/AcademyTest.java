package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.*;

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
        Academy academy = academyRepository.save(
            Academy.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .build()
        );
        student.applyAcademy(academy);
        academy.acceptStudent(student);

        assertThat(academy.getStudents().contains(student)).isTrue();
        assertThat(academy.getApplyingStudents().contains(student)).isFalse();
    }

    @Test
    void acceptStudent_NotApplied_ExceptionThrown() {
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
        Academy academy = academyRepository.save(
            Academy.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .build()
        );

        assertThatThrownBy(() -> academy.acceptStudent(student))
            .isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void acceptTeacher() {
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
        Academy academy = academyRepository.save(
            Academy.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .build()
        );
        teacher.applyAcademy(academy);
        academy.acceptTeacher(teacher);

        assertThat(academy.getTeachers().contains(teacher)).isTrue();
        assertThat(academy.getApplyingTeachers().contains(teacher)).isFalse();
    }

    @Test
    void acceptTeacher_NotApplied_ExceptionThrown() {
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
        Academy academy = academyRepository.save(
            Academy.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .build()
        );

        assertThatThrownBy(() -> academy.acceptTeacher(teacher))
            .isInstanceOf(TeacherNotFoundException.class);
    }
}