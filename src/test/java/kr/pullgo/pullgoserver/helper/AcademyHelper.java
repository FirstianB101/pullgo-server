package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;

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

    private static final String ARBITRARY_NAME = "퍼스티안 학원";
    private static final String ARBITRARY_PHONE = "021234567";
    private static final String ARBITRARY_ADDRESS = "서울특별시 노원구 월계1동 광운로 20";

    public static Academy anAcademy() {
        Academy academy = Academy.builder()
            .name(ARBITRARY_NAME)
            .phone(ARBITRARY_PHONE)
            .address(ARBITRARY_ADDRESS)
            .build();
        academy.setId(0L);

        Teacher owner = aTeacher();
        academy.addTeacher(owner);
        academy.setOwner(owner);

        return academy;
    }

    public static AcademyDto.Create anAcademyCreateDto() {
        return Create.builder()
            .name(ARBITRARY_NAME)
            .phone(ARBITRARY_PHONE)
            .address(ARBITRARY_ADDRESS)
            .ownerId(0L)
            .build();
    }

    public static AcademyDto.Update anAcademyUpdateDto() {
        return Update.builder()
            .name(ARBITRARY_NAME)
            .phone(ARBITRARY_PHONE)
            .address(ARBITRARY_ADDRESS)
            .ownerId(0L)
            .build();
    }

    public static Result anAcademyResultDto() {
        return Result.builder()
            .id(0L)
            .name(ARBITRARY_NAME)
            .phone(ARBITRARY_PHONE)
            .address(ARBITRARY_ADDRESS)
            .ownerId(0L)
            .build();
    }

    public static AcceptTeacher anAcademyAcceptTeacherDto() {
        return AcceptTeacher.builder()
            .teacherId(0L)
            .build();
    }

    public static KickTeacher anAcademyKickTeacherDto() {
        return KickTeacher.builder()
            .teacherId(0L)
            .build();
    }

    public static AcceptStudent anAcademyAcceptStudentDto() {
        return AcceptStudent.builder()
            .studentId(0L)
            .build();
    }

    public static KickStudent anAcademyKickStudentDto() {
        return KickStudent.builder()
            .studentId(0L)
            .build();
    }

}
