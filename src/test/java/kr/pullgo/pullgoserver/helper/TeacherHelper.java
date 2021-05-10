package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountCreateDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountResultDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccountUpdateDto;

import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.persistence.model.Teacher;

public class TeacherHelper {

    public static Teacher aTeacher() {
        return new Teacher();
    }

    public static TeacherDto.Create aTeacherCreateDto() {
        return TeacherDto.Create.builder()
            .account(anAccountCreateDto())
            .build();
    }

    public static TeacherDto.Update aTeacherUpdateDto() {
        return TeacherDto.Update.builder()
            .account(anAccountUpdateDto())
            .build();
    }

    public static TeacherDto.Result aTeacherResultDto() {
        return TeacherDto.Result.builder()
            .id(0L)
            .account(anAccountResultDto())
            .build();
    }

    public static TeacherDto.ApplyAcademy aTeacherApplyAcademyDto() {
        return TeacherDto.ApplyAcademy.builder()
            .academyId(0L)
            .build();
    }

    public static TeacherDto.RemoveAppliedAcademy aTeacherRemoveAppliedAcademyDto() {
        return TeacherDto.RemoveAppliedAcademy.builder()
            .academyId(0L)
            .build();
    }

    public static TeacherDto.ApplyClassroom aTeacherApplyClassroomDto() {
        return TeacherDto.ApplyClassroom.builder()
            .classroomId(0L)
            .build();
    }

    public static TeacherDto.RemoveAppliedClassroom aTeacherRemoveAppliedClassroomDto() {
        return TeacherDto.RemoveAppliedClassroom.builder()
            .classroomId(0L)
            .build();
    }

}
