package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademy;

import kr.pullgo.pullgoserver.dto.ClassroomDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;

public class ClassroomHelper {

    private static final String ARBITRARY_NAME = "컴퓨터네트워크 최웅철 (월수금)";

    public static Classroom aClassroom() {
        Classroom classroom = Classroom.builder()
            .name(ARBITRARY_NAME)
            .build();
        classroom.setId(0L);
        classroom.setAcademy(anAcademy());
        return classroom;
    }

    public static ClassroomDto.Create aClassroomCreateDto() {
        return ClassroomDto.Create.builder()
            .name(ARBITRARY_NAME)
            .build();
    }

    public static ClassroomDto.Update aClassroomUpdateDto() {
        return ClassroomDto.Update.builder()
            .name(ARBITRARY_NAME)
            .build();
    }

    public static ClassroomDto.Result aClassroomResultDto() {
        return ClassroomDto.Result.builder()
            .id(0L)
            .academyId(0L)
            .name(ARBITRARY_NAME)
            .build();
    }

    public static ClassroomDto.AcceptTeacher aClassroomAcceptTeacherDto() {
        return ClassroomDto.AcceptTeacher.builder()
            .teacherId(0L)
            .build();
    }

    public static ClassroomDto.KickTeacher aClassroomKickTeacherDto() {
        return ClassroomDto.KickTeacher.builder()
            .teacherId(0L)
            .build();
    }

    public static ClassroomDto.AcceptStudent aClassroomAcceptStudentDto() {
        return ClassroomDto.AcceptStudent.builder()
            .studentId(0L)
            .build();
    }

    public static ClassroomDto.KickStudent aClassroomKickStudentDto() {
        return ClassroomDto.KickStudent.builder()
            .studentId(0L)
            .build();
    }

}
