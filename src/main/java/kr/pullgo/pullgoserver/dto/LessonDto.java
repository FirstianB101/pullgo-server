package kr.pullgo.pullgoserver.dto;

import com.sun.istack.NotNull;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface LessonDto {

    static LessonDto.Result mapFromEntity(Lesson lesson) {
        return Result.builder()
            .id(lesson.getId())
            .schedule(ScheduleDto.mapFromEntity(lesson.getSchedule()))
            .name(lesson.getName())
            .classroomId(lesson.getClassroom().getId())
            .build();
    }

    static Lesson mapToEntity(LessonDto.Create dto) {
        Lesson lesson = Lesson.builder()
            .name(dto.getName())
            .build();
        lesson.setSchedule(ScheduleDto.mapToEntity(dto.getSchedule()));
        return lesson;
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private Long classroomId;

        @NonNull
        private String name;

        @NotNull
        private ScheduleDto.Create schedule;
    }

    @Data
    @Builder
    class Update {

        private String name;

        private ScheduleDto.Update schedule;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private Long classroomId;

        @NonNull
        private String name;

        @NotNull
        private ScheduleDto.Result schedule;
    }
}
