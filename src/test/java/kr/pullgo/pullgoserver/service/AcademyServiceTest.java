package kr.pullgo.pullgoserver.service;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademy;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyAcceptStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyAcceptTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyCreateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyKickStudentDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyKickTeacherDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyUpdateDto;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudent;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.dto.mapper.AcademyDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AcademyServiceTest {

    @Mock
    private AcademyRepository academyRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AccountRepository accountRepository;

    private AcademyService academyService;

    @BeforeEach
    void setUp() {
        academyService = new AcademyService(new AcademyDtoMapper(), academyRepository,
            teacherRepository, studentRepository);
    }

    @Nested
    class Create {

        @Test
        void createAcademy() {
            // Given
            AcademyDto.Create dto = AcademyDto.Create.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .ownerId(0L)
                .build();

            given(academyRepository.save(any()))
                .will(i -> ((Academy) i.getArgument(0)).withId(0L));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(aTeacher().withId(0L)));

            // When
            AcademyDto.Result result = academyService.create(dto);

            // Then
            assertThat(result.getName()).isEqualTo("Test academy");
            assertThat(result.getPhone()).isEqualTo("01012345678");
            assertThat(result.getAddress()).isEqualTo("Seoul");
        }

        @Test
        void createAcademy_InvalidOwnerId_ExceptionThrown() {
            // Given
            given(teacherRepository.findById(0L))
                .willReturn(Optional.empty());

            // When
            AcademyDto.Create dto = anAcademyCreateDto().withOwnerId(0L);
            Throwable thrown = catchThrowable(() -> academyService.create(dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class Update {

        @Test
        void updateAcademy() {
            // Given
            Teacher oldOwner = aTeacher().withId(1L);
            Teacher newOwner = aTeacher().withId(2L);

            Academy entity = Academy.builder()
                .name("Before")
                .phone("01000000000")
                .address("Zottopia")
                .build();
            entity.setId(0L);

            entity.addTeacher(oldOwner);
            entity.addTeacher(newOwner);

            entity.setOwner(oldOwner);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(entity));

            given(academyRepository.save(any()))
                .will(i -> i.getArgument(0));

            given(teacherRepository.findById(2L))
                .willReturn(Optional.of(newOwner));

            // When
            AcademyDto.Update dto = AcademyDto.Update.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .ownerId(2L)
                .build();

            AcademyDto.Result result = academyService.update(0L, dto);

            // Then
            assertThat(result.getName()).isEqualTo("Test academy");
            assertThat(result.getPhone()).isEqualTo("01012345678");
            assertThat(result.getAddress()).isEqualTo("Seoul");
            assertThat(result.getOwnerId()).isEqualTo(2);
        }

        @Test
        void updateAcademy_InvalidAcademyId_ExceptionThrown() {
            // Given
            AcademyDto.Update dto = anAcademyUpdateDto();

            given(academyRepository.findById(1L))
                .willReturn(Optional.empty());

            // When
            Throwable thrown = catchThrowable(() -> academyService.update(1L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }

        @Test
        void updateAcademy_InvalidOwnerId_ExceptionThrown() {
            // Given
            Academy entity = Academy.builder()
                .name("Before")
                .phone("01000000000")
                .address("Zottopia")
                .build();
            entity.setId(0L);

            Teacher teacher = aTeacher().withId(1L);
            entity.addTeacher(teacher);
            entity.setOwner(teacher);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(entity));

            given(teacherRepository.findById(2L))
                .willReturn(Optional.empty());

            // When
            AcademyDto.Update dto = anAcademyUpdateDto().withOwnerId(2L);
            Throwable thrown = catchThrowable(() -> academyService.update(0L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        void deleteAcademy() {
            // Given
            given(academyRepository.findById(1L))
                .willReturn(Optional.of(anAcademy().withId(1L)));

            given(academyRepository.removeById(1L))
                .willReturn(1);

            // When
            academyService.delete(1L);

            // Then
            verify(academyRepository).removeById(1L);
        }

        @Test
        void deleteAcademy_InvalidAcademyId_ExceptionThrown() {
            // Given
            given(academyRepository.findById(1L))
                .willReturn(Optional.of(anAcademy().withId(1L)));

            given(academyRepository.removeById(1L))
                .willReturn(0);

            // When
            Throwable thrown = catchThrowable(() -> academyService.delete(1L));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class Get {

        @Test
        void getAcademy() {
            // Given
            Academy entity = Academy.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .build();
            entity.setId(0L);

            Teacher teacher = aTeacher().withId(1L);
            entity.addTeacher(teacher);
            entity.setOwner(teacher);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(entity));

            // When
            AcademyDto.Result result = academyService.read(0L);

            // Then
            assertThat(result.getName()).isEqualTo("Test academy");
            assertThat(result.getPhone()).isEqualTo("01012345678");
            assertThat(result.getAddress()).isEqualTo("Seoul");
            assertThat(result.getOwnerId()).isEqualTo(1L);
        }

        @Test
        void getAcademy_InvalidAcademyId_ExceptionThrown() {
            // Given
            given(academyRepository.findById(0L))
                .willReturn(Optional.empty());

            // When
            Throwable thrown = catchThrowable(() -> academyService.read(0L));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class AcceptTeacher {


        @Test
        void acceptTeacher() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Teacher teacher = aTeacher().withId(0L);

            teacher.applyAcademy(academy);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.AcceptTeacher dto = anAcademyAcceptTeacherDto().withTeacherId(0L);
            academyService.acceptTeacher(0L, dto);

            // Then
            assertThat(academy.getTeachers()).contains(teacher);
            assertThat(academy.getApplyingTeachers()).doesNotContain(teacher);

            assertThat(teacher.getAcademies()).containsOnly(academy);
            assertThat(teacher.getAppliedAcademies()).doesNotContain(academy);
        }

        @Test
        void acceptTeacher_TeacherNotApplied_ExceptionThrown() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Teacher teacher = aTeacher().withId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.AcceptTeacher dto = anAcademyAcceptTeacherDto().withTeacherId(0L);
            Throwable thrown = catchThrowable(() -> academyService.acceptTeacher(0L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not applied teacher");
        }
    }

    @Nested
    class KickTeacher {

        @Test
        void kickTeacher() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Teacher teacher = aTeacher().withId(0L);

            teacher.applyAcademy(academy);
            academy.acceptTeacher(teacher);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.KickTeacher dto = anAcademyKickTeacherDto().withTeacherId(0L);
            academyService.kickTeacher(0L, dto);

            // Then
            assertThat(academy.getTeachers()).doesNotContain(teacher);
            assertThat(teacher.getAcademies()).doesNotContain(academy);
        }

        @Test
        void kickTeacher_TeacherNotEnrolled_ExceptionThrown() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Teacher teacher = aTeacher().withId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.KickTeacher dto = anAcademyKickTeacherDto().withTeacherId(0L);
            Throwable thrown = catchThrowable(() -> academyService.kickTeacher(0L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not enrolled teacher");
        }
    }

    @Nested
    class AcceptStudent {

        @Test
        void acceptStudent() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Student student = aStudent().withId(0L);

            student.applyAcademy(academy);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.AcceptStudent dto = anAcademyAcceptStudentDto().withStudentId(0L);
            academyService.acceptStudent(0L, dto);

            // Then
            assertThat(academy.getStudents()).containsOnly(student);
            assertThat(academy.getApplyingStudents()).doesNotContain(student);

            assertThat(student.getAcademies()).containsOnly(academy);
            assertThat(student.getAppliedAcademies()).doesNotContain(academy);
        }

        @Test
        void acceptStudent_StudentNotApplied_ExceptionThrown() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Student student = aStudent().withId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.AcceptStudent dto = anAcademyAcceptStudentDto().withStudentId(0L);
            Throwable thrown = catchThrowable(() -> academyService.acceptStudent(0L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not applied student");
        }
    }

    @Nested
    class KickStudent {

        @Test
        void kickStudent() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Student student = aStudent().withId(0L);

            student.applyAcademy(academy);
            academy.acceptStudent(student);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.KickStudent dto = anAcademyKickStudentDto().withStudentId(0L);
            academyService.kickStudent(0L, dto);

            // Then
            assertThat(academy.getStudents()).doesNotContain(student);
            assertThat(student.getAcademies()).doesNotContain(academy);
        }

        @Test
        void kickStudent_StudentNotEnrolled_ExceptionThrown() {
            // Given
            Academy academy = anAcademy().withId(0L);
            Student student = aStudent().withId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.KickStudent dto = anAcademyKickStudentDto().withStudentId(0L);
            Throwable thrown = catchThrowable(() -> academyService.kickStudent(0L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not enrolled student");
        }
    }
}