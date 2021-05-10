package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aSchedule;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aScheduleCreateDto;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aScheduleResultDto;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aScheduleUpdateDto;

import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.persistence.model.Lesson;

public class LessonHelper {

    public static Lesson aLesson() {
        Lesson lesson = Lesson.builder()
            .name("test name")
            .build();
        lesson.setId(0L);
        lesson.setClassroom(aClassroom());
        lesson.setSchedule(aSchedule());
        return lesson;
    }

    public static LessonDto.Create aLessonCreateDto() {
        return LessonDto.Create.builder()
            .classroomId(0L)
            .name("test name")
            .schedule(aScheduleCreateDto())
            .build();
    }

    public static LessonDto.Update aLessonUpdateDto() {
        return LessonDto.Update.builder()
            .name("test name")
            .schedule(aScheduleUpdateDto())
            .build();
    }

    public static LessonDto.Result aLessonResultDto() {
        return LessonDto.Result.builder()
            .id(0L)
            .classroomId(0L)
            .name("test name")
            .schedule(aScheduleResultDto())
            .build();
    }

}
