package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.TeacherHelper.teacherWithId;

import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.dto.AcademyDto.AcceptStudent;
import kr.pullgo.pullgoserver.dto.AcademyDto.AcceptTeacher;
import kr.pullgo.pullgoserver.dto.AcademyDto.Create;
import kr.pullgo.pullgoserver.dto.AcademyDto.KickStudent;
import kr.pullgo.pullgoserver.dto.AcademyDto.KickTeacher;
import kr.pullgo.pullgoserver.dto.AcademyDto.Result;
import kr.pullgo.pullgoserver.dto.AcademyDto.Update;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Teacher;

public class AcademyHelper {

    public static Academy withId(Academy entity, Long id) {
        entity.setId(id);
        return entity;
    }

    public static Result academyResultDtoWithId(Long id) {
        return Result.builder()
            .id(id)
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .ownerId(0L)
            .build();
    }

    public static Create academyCreateDto() {
        return Create.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .ownerId(0L)
            .build();
    }

    public static Create academyCreateDtoWithOwnerId(long ownerId) {
        return AcademyDto.Create.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .ownerId(ownerId)
            .build();
    }

    public static Update academyUpdateDtoWithOwnerId(long ownerId) {
        return AcademyDto.Update.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .ownerId(ownerId)
            .build();
    }

    public static Update academyUpdateDto() {
        return Update.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .ownerId(0L)
            .build();
    }

    public static Academy academyWithId(Long id) {
        Academy academy = Academy.builder()
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .build();
        academy.setId(id);

        Teacher teacher = teacherWithId(0L);
        academy.addTeacher(teacher);
        academy.setOwner(teacher);

        return academy;
    }

    public static AcceptTeacher acceptTeacherDto() {
        return AcceptTeacher.builder()
            .teacherId(0L)
            .build();
    }

    public static AcceptTeacher acceptTeacherDtoWithTeacherId(Long teacherId) {
        return AcceptTeacher.builder()
            .teacherId(teacherId)
            .build();
    }

    public static KickTeacher kickTeacherDto() {
        return KickTeacher.builder()
            .teacherId(0L)
            .build();
    }

    public static KickTeacher kickTeacherDtoWithTeacherId(Long teacherId) {
        return KickTeacher.builder()
            .teacherId(teacherId)
            .build();
    }

    public static AcceptStudent acceptStudentDto() {
        return AcceptStudent.builder()
            .studentId(0L)
            .build();
    }

    public static AcceptStudent acceptStudentDtoWithStudentId(Long studentId) {
        return AcceptStudent.builder()
            .studentId(studentId)
            .build();
    }

    public static KickStudent kickStudentDto() {
        return KickStudent.builder()
            .studentId(0L)
            .build();
    }

    public static KickStudent kickStudentDtoWithStudentId(Long studentId) {
        return KickStudent.builder()
            .studentId(studentId)
            .build();
    }
}
