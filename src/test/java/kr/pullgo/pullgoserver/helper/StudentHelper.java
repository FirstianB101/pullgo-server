package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountCreateDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountResultDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountUpdateDto;

import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.persistence.model.Student;

public class StudentHelper {

    private static final String ARBITRARY_PARENT_PHONE = "01098765432";
    private static final String ARBITRARY_SCHOOL_NAME = "광운전자공업고등학교";
    private static final int ARBITRARY_SCHOOL_YEAR = 3;

    public static Student aStudent() {
        Student student = Student.builder()
            .parentPhone(ARBITRARY_PARENT_PHONE)
            .schoolName(ARBITRARY_SCHOOL_NAME)
            .schoolYear(ARBITRARY_SCHOOL_YEAR)
            .build();
        student.setId(0L);
        student.setAccount(anAccount());
        return student;
    }

    public static StudentDto.Create aStudentCreateDto() {
        return StudentDto.Create.builder()
            .parentPhone(ARBITRARY_PARENT_PHONE)
            .schoolName(ARBITRARY_SCHOOL_NAME)
            .schoolYear(ARBITRARY_SCHOOL_YEAR)
            .account(anAccountCreateDto())
            .build();
    }

    public static StudentDto.Update aStudentUpdateDto() {
        return StudentDto.Update.builder()
            .parentPhone(ARBITRARY_PARENT_PHONE)
            .schoolName(ARBITRARY_SCHOOL_NAME)
            .schoolYear(ARBITRARY_SCHOOL_YEAR)
            .account(anAccountUpdateDto())
            .build();
    }

    public static StudentDto.Result aStudentResultDto() {
        return StudentDto.Result.builder()
            .id(0L)
            .parentPhone(ARBITRARY_PARENT_PHONE)
            .schoolName(ARBITRARY_SCHOOL_NAME)
            .schoolYear(ARBITRARY_SCHOOL_YEAR)
            .account(anAccountResultDto())
            .build();
    }

    public static StudentDto.ApplyAcademy aStudentApplyAcademyDto() {
        return StudentDto.ApplyAcademy.builder()
            .academyId(0L)
            .build();
    }

    public static StudentDto.RemoveAppliedAcademy aStudentRemoveAppliedAcademyDto() {
        return StudentDto.RemoveAppliedAcademy.builder()
            .academyId(0L)
            .build();
    }

    public static StudentDto.ApplyClassroom aStudentApplyClassroomDto() {
        return StudentDto.ApplyClassroom.builder()
            .classroomId(0L)
            .build();
    }

    public static StudentDto.RemoveAppliedClassroom aStudentRemoveAppliedClassroomDto() {
        return StudentDto.RemoveAppliedClassroom.builder()
            .classroomId(0L)
            .build();
    }

}
