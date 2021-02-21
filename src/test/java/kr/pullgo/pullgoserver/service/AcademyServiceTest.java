package kr.pullgo.pullgoserver.service;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyUpdateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyWithId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptTeacherDtoWithTeacherId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickTeacherDtoWithTeacherId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.withId;
import static kr.pullgo.pullgoserver.helper.StudentHelper.studentWithId;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.teacherWithId;
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
                .build();

            given(academyRepository.save(any()))
                .will(i -> withId(i.getArgument(0), 0L));

            // When
            AcademyDto.Result result = academyService.createAcademy(dto);

            // Then
            assertThat(result.getName()).isEqualTo("Test academy");
            assertThat(result.getPhone()).isEqualTo("01012345678");
            assertThat(result.getAddress()).isEqualTo("Seoul");
        }
    }

    @Nested
    class Update {

        @Test
        void updateAcademy() {
            // Given
            Academy entity = Academy.builder()
                .name("Before")
                .phone("01000000000")
                .address("Zottopia")
                .build();
            entity.setId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(entity));

            given(academyRepository.save(any()))
                .will(i -> i.getArgument(0));

            // When
            AcademyDto.Update dto = AcademyDto.Update.builder()
                .name("Test academy")
                .phone("01012345678")
                .address("Seoul")
                .build();

            AcademyDto.Result result = academyService.updateAcademy(0L, dto);

            // Then
            assertThat(result.getName()).isEqualTo("Test academy");
            assertThat(result.getPhone()).isEqualTo("01012345678");
            assertThat(result.getAddress()).isEqualTo("Seoul");
        }

        @Test
        void updateAcademy_InvalidAcademyId_ExceptionThrown() {
            // Given
            AcademyDto.Update dto = academyUpdateDto();

            given(academyRepository.findById(1L))
                .willReturn(Optional.empty());

            // When
            Throwable thrown = catchThrowable(() -> academyService.updateAcademy(1L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        void deleteAcademy() {
            // Given
            given(academyRepository.removeById(1L))
                .willReturn(1);

            // When
            academyService.deleteAcademy(1L);

            // Then
            verify(academyRepository).removeById(1L);
        }

        @Test
        void deleteAcademy_InvalidAcademyId_ExceptionThrown() {
            // Given
            given(academyRepository.removeById(1L))
                .willReturn(0);

            // When
            Throwable thrown = catchThrowable(() -> academyService.deleteAcademy(1L));

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

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(entity));

            // When
            AcademyDto.Result result = academyService.getAcademy(0L);

            // Then
            assertThat(result.getName()).isEqualTo("Test academy");
            assertThat(result.getPhone()).isEqualTo("01012345678");
            assertThat(result.getAddress()).isEqualTo("Seoul");
        }

        @Test
        void getAcademy_InvalidAcademyId_ExceptionThrown() {
            // Given
            given(academyRepository.findById(0L))
                .willReturn(Optional.empty());

            // When
            Throwable thrown = catchThrowable(() -> academyService.getAcademy(0L));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class AcceptTeacher {


        @Test
        void acceptTeacher() {
            // Given
            Academy academy = academyWithId(0L);
            Teacher teacher = teacherWithId(0L);

            teacher.applyAcademy(academy);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.AcceptTeacher dto = acceptTeacherDtoWithTeacherId(0L);
            academyService.acceptTeacher(0L, dto);

            // Then
            assertThat(academy.getTeachers()).containsOnly(teacher);
            assertThat(academy.getApplyingTeachers()).doesNotContain(teacher);

            assertThat(teacher.getAcademies()).containsOnly(academy);
            assertThat(teacher.getAppliedAcademies()).doesNotContain(academy);
        }

        @Test
        void acceptTeacher_TeacherNotApplied_ExceptionThrown() {
            // Given
            Academy academy = academyWithId(0L);
            Teacher teacher = teacherWithId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.AcceptTeacher dto = acceptTeacherDtoWithTeacherId(0L);
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
            Academy academy = academyWithId(0L);
            Teacher teacher = teacherWithId(0L);

            teacher.applyAcademy(academy);
            academy.acceptTeacher(teacher);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.KickTeacher dto = kickTeacherDtoWithTeacherId(0L);
            academyService.kickTeacher(0L, dto);

            // Then
            assertThat(academy.getTeachers()).doesNotContain(teacher);
            assertThat(teacher.getAcademies()).doesNotContain(academy);
        }

        @Test
        void kickTeacher_TeacherNotEnrolled_ExceptionThrown() {
            // Given
            Academy academy = academyWithId(0L);
            Teacher teacher = teacherWithId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(teacherRepository.findById(0L))
                .willReturn(Optional.of(teacher));

            // When
            AcademyDto.KickTeacher dto = kickTeacherDtoWithTeacherId(0L);
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
            Academy academy = academyWithId(0L);
            Student student = studentWithId(0L);

            student.applyAcademy(academy);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.AcceptStudent dto = acceptStudentDtoWithStudentId(0L);
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
            Academy academy = academyWithId(0L);
            Student student = studentWithId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.AcceptStudent dto = acceptStudentDtoWithStudentId(0L);
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
            Academy academy = academyWithId(0L);
            Student student = studentWithId(0L);

            student.applyAcademy(academy);
            academy.acceptStudent(student);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.KickStudent dto = kickStudentDtoWithStudentId(0L);
            academyService.kickStudent(0L, dto);

            // Then
            assertThat(academy.getStudents()).doesNotContain(student);
            assertThat(student.getAcademies()).doesNotContain(academy);
        }

        @Test
        void kickStudent_StudentNotEnrolled_ExceptionThrown() {
            // Given
            Academy academy = academyWithId(0L);
            Student student = studentWithId(0L);

            given(academyRepository.findById(0L))
                .willReturn(Optional.of(academy));

            given(studentRepository.findById(0L))
                .willReturn(Optional.of(student));

            // When
            AcademyDto.KickStudent dto = kickStudentDtoWithStudentId(0L);
            Throwable thrown = catchThrowable(() -> academyService.kickStudent(0L, dto));

            // Then
            assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not enrolled student");
        }
    }
}