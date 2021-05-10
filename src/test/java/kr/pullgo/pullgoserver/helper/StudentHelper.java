package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountCreateDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountResultDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountUpdateDto;

import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.persistence.model.Student;

public class StudentHelper {

    public static Student aStudent() {
        Student student = Student.builder()
            .parentPhone("01012345678")
            .schoolName("KwangWoon")
            .schoolYear(3)
            .build();
        student.setId(0L);
        student.setAccount(anAccount());
        return student;
    }

    public static StudentDto.Create aStudentCreateDto() {
        return StudentDto.Create.builder()
            .parentPhone("01098765432")
            .schoolName("test school")
            .schoolYear(1)
            .account(anAccountCreateDto())
            .build();
    }

    public static StudentDto.Update aStudentUpdateDto() {
        return StudentDto.Update.builder()
            .parentPhone("01098765432")
            .schoolName("test school")
            .schoolYear(1)
            .account(anAccountUpdateDto())
            .build();
    }

    public static StudentDto.Result aStudentResultDto() {
        return StudentDto.Result.builder()
            .id(0L)
            .parentPhone("01098765432")
            .schoolName("test school")
            .schoolYear(1)
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
