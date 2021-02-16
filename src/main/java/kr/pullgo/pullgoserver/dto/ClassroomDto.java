package kr.pullgo.pullgoserver.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface ClassroomDto {

    static ClassroomDto.Result mapFromEntity(Classroom classroom) {
        return Result.builder()
            .id(classroom.getId())
            .name(classroom.getName())
            .academyId(classroom.getAcademy().getId())
            .build();
    }

    static Classroom mapToEntity(ClassroomDto.Create dto) {
        return Classroom.builder()
            .name(dto.getName())
            .build();
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private String name;

        @NonNull
        private Long academyId;
    }

    @Data
    @Builder
    class Update {

        private String name;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private Long academyId;

        @NonNull
        private String name;
    }


    @Data
    @Builder
    class AcceptTeacher {

        @NonNull
        private Long teacherId;

        @JsonCreator
        public AcceptTeacher(@NonNull Long teacherId) {
            this.teacherId = teacherId;
        }
    }

    @Data
    @Builder
    class KickTeacher {

        @NonNull
        private Long teacherId;

        @JsonCreator
        public KickTeacher(@NonNull Long teacherId) {
            this.teacherId = teacherId;
        }
    }

    @Data
    @Builder
    class AcceptStudent {

        @NonNull
        private Long studentId;

        @JsonCreator
        public AcceptStudent(@NonNull Long studentId) {
            this.studentId = studentId;
        }
    }

    @Data
    @Builder
    class KickStudent {

        @NonNull
        private Long studentId;

        @JsonCreator
        public KickStudent(@NonNull Long studentId) {
            this.studentId = studentId;
        }
    }
}
