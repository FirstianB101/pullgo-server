package kr.pullgo.pullgoserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.dto.AcademyDto.AcceptStudent;
import kr.pullgo.pullgoserver.dto.AcademyDto.AcceptTeacher;
import kr.pullgo.pullgoserver.dto.AcademyDto.KickStudent;
import kr.pullgo.pullgoserver.dto.AcademyDto.KickTeacher;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AcademyRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AcademyServiceTest {

    private AcademyService academyService;
    private AcademyRepository academyRepository;
    private TeacherRepository teacherRepository;
    private StudentRepository studentRepository;

    @BeforeEach
    void beforeEach() {
        academyRepository = mock(AcademyRepository.class);
        teacherRepository = mock(TeacherRepository.class);
        studentRepository = mock(StudentRepository.class);
        academyService = new AcademyService(academyRepository, teacherRepository,
            studentRepository);
    }

    @Test
    void createAcademy() {
        // Given
        AcademyDto.Create dto = AcademyDto.Create.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();

        given(academyRepository.save(any()))
            .will(i -> {
                Academy entity = i.getArgument(0);
                entity.setId(0L);
                return entity;
            });

        // When
        AcademyDto.Result result = academyService.createAcademy(dto);

        // Then
        assertThat(result.getName()).isEqualTo(dto.getName());
        assertThat(result.getPhone()).isEqualTo(dto.getPhone());
        assertThat(result.getAddress()).isEqualTo(dto.getAddress());
    }

    @Test
    void updateAcademy() {
        // Given
        AcademyDto.Update dto = AcademyDto.Update.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();

        Academy entity = Academy.builder()
            .name("Before")
            .phone("010-0000-0000")
            .address("Zottopia")
            .build();
        entity.setId(0L);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(entity));

        given(academyRepository.save(any()))
            .will(i -> i.getArgument(0));

        // When
        AcademyDto.Result result = academyService.updateAcademy(0L, dto);

        // Then
        assertThat(result.getName()).isEqualTo(dto.getName());
        assertThat(result.getPhone()).isEqualTo(dto.getPhone());
        assertThat(result.getAddress()).isEqualTo(dto.getAddress());
    }

    @Test
    void updateAcademy_InvalidAcademyId_ExceptionThrown() {
        // Given
        AcademyDto.Update dto = AcademyDto.Update.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> academyService.updateAcademy(1L, dto));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteAcademy() {
        // Given
        given(academyRepository.removeById(anyLong()))
            .willReturn(1);

        // When
        academyService.deleteAcademy(1L);

        // Then
        verify(academyRepository).removeById(1L);
    }

    @Test
    void deleteAcademy_InvalidAcademyId_ExceptionThrown() {
        // Given
        given(academyRepository.removeById(anyLong()))
            .willReturn(0);

        // When
        Throwable thrown = catchThrowable(() -> academyService.deleteAcademy(1L));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getAcademy() {
        // Given
        Academy entity = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        entity.setId(0L);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(entity));

        // When
        AcademyDto.Result result = academyService.getAcademy(0L);

        // Then
        assertThat(result.getName()).isEqualTo(entity.getName());
        assertThat(result.getPhone()).isEqualTo(entity.getPhone());
        assertThat(result.getAddress()).isEqualTo(entity.getAddress());
    }

    @Test
    void getAcademy_InvalidAcademyId_ExceptionThrown() {
        // Given
        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> academyService.getAcademy(0L));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getAcademies() {
        // Given
        Academy entityA = Academy.builder()
            .name("A")
            .phone("010-1111-1111")
            .address("Aeoul")
            .build();
        entityA.setId(0L);

        Academy entityB = Academy.builder()
            .name("B")
            .phone("010-2222-2222")
            .address("Beoul")
            .build();
        entityB.setId(1L);

        List<Academy> entities = Lists.list(entityA, entityB);

        given(academyRepository.findAll())
            .willReturn(entities);

        // When
        List<AcademyDto.Result> results = academyService.getAcademies();

        // Then
        for (int i = 0; i < entities.size(); i++) {
            Academy entity = entities.get(i);
            AcademyDto.Result result = results.get(i);

            assertThat(result.getName()).isEqualTo(entity.getName());
            assertThat(result.getPhone()).isEqualTo(entity.getPhone());
            assertThat(result.getAddress()).isEqualTo(entity.getAddress());
        }
    }

    @Test
    void acceptTeacher() {
        // Given
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.applyAcademy(academy);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(teacherRepository.findById(anyLong()))
            .willReturn(Optional.of(teacher));

        // When
        AcademyDto.AcceptTeacher dto = AcceptTeacher.builder()
            .teacherId(0L)
            .build();
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
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Teacher teacher = new Teacher();
        teacher.setId(0L);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(teacherRepository.findById(anyLong()))
            .willReturn(Optional.of(teacher));

        // When
        AcademyDto.AcceptTeacher dto = AcceptTeacher.builder()
            .teacherId(0L)
            .build();
        Throwable thrown = catchThrowable(() -> academyService.acceptTeacher(0L, dto));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Not applied teacher");
    }

    @Test
    void kickTeacher() {
        // Given
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Teacher teacher = new Teacher();
        teacher.setId(0L);

        teacher.applyAcademy(academy);
        academy.acceptTeacher(teacher);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(teacherRepository.findById(anyLong()))
            .willReturn(Optional.of(teacher));

        // When
        AcademyDto.KickTeacher dto = KickTeacher.builder()
            .teacherId(0L)
            .build();
        academyService.kickTeacher(0L, dto);

        // Then
        assertThat(academy.getTeachers()).doesNotContain(teacher);
        assertThat(teacher.getAcademies()).doesNotContain(academy);
    }

    @Test
    void kickTeacher_TeacherNotEnrolled_ExceptionThrown() {
        // Given
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Teacher teacher = new Teacher();
        teacher.setId(0L);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(teacherRepository.findById(anyLong()))
            .willReturn(Optional.of(teacher));

        // When
        AcademyDto.KickTeacher dto = KickTeacher.builder()
            .teacherId(0L)
            .build();
        Throwable thrown = catchThrowable(() -> academyService.kickTeacher(0L, dto));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Not enrolled teacher");
    }

    @Test
    void acceptStudent() {
        // Given
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Student student = Student.builder()
            .parentPhone("010-0000-1111")
            .schoolName("KwangWoon")
            .schoolYear(3)
            .build();
        student.setId(0L);
        student.applyAcademy(academy);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(studentRepository.findById(anyLong()))
            .willReturn(Optional.of(student));

        // When
        AcademyDto.AcceptStudent dto = AcceptStudent.builder()
            .studentId(0L)
            .build();
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
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Student student = Student.builder()
            .parentPhone("010-0000-1111")
            .schoolName("KwangWoon")
            .schoolYear(3)
            .build();
        student.setId(0L);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(studentRepository.findById(anyLong()))
            .willReturn(Optional.of(student));

        // When
        AcademyDto.AcceptStudent dto = AcceptStudent.builder()
            .studentId(0L)
            .build();
        Throwable thrown = catchThrowable(() -> academyService.acceptStudent(0L, dto));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Not applied student");
    }

    @Test
    void kickStudent() {
        // Given
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Student student = Student.builder()
            .parentPhone("010-0000-1111")
            .schoolName("KwangWoon")
            .schoolYear(3)
            .build();
        student.setId(0L);

        student.applyAcademy(academy);
        academy.acceptStudent(student);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(studentRepository.findById(anyLong()))
            .willReturn(Optional.of(student));

        // When
        AcademyDto.KickStudent dto = KickStudent.builder()
            .studentId(0L)
            .build();
        academyService.kickStudent(0L, dto);

        // Then
        assertThat(academy.getStudents()).doesNotContain(student);
        assertThat(student.getAcademies()).doesNotContain(academy);
    }

    @Test
    void kickStudent_StudentNotEnrolled_ExceptionThrown() {
        // Given
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();
        academy.setId(0L);

        Student student = Student.builder()
            .parentPhone("010-0000-1111")
            .schoolName("KwangWoon")
            .schoolYear(3)
            .build();
        student.setId(0L);

        given(academyRepository.findById(anyLong()))
            .willReturn(Optional.of(academy));

        given(studentRepository.findById(anyLong()))
            .willReturn(Optional.of(student));

        // When
        AcademyDto.KickStudent dto = KickStudent.builder()
            .studentId(0L)
            .build();
        Throwable thrown = catchThrowable(() -> academyService.kickStudent(0L, dto));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Not enrolled student");
    }
}