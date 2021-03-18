package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface ClassroomDto {

    @Data
    @Builder
    class Create {

        @NotNull
        private String name;

        @NotNull
        private Long academyId;

        @NotNull
        private Long creatorId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    class Update {

        private String name;

        public Update(String name) {
            this.name = name;
        }
    }

    @Data
    @Builder
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private Long academyId;

        @NotNull
        private String name;
    }


    @Data
    @Builder
    @NoArgsConstructor
    class AcceptTeacher {

        @NotNull
        private Long teacherId;

        public AcceptTeacher(@NotNull Long teacherId) {
            this.teacherId = teacherId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class KickTeacher {

        @NotNull
        private Long teacherId;

        public KickTeacher(@NotNull Long teacherId) {
            this.teacherId = teacherId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class AcceptStudent {

        @NotNull
        private Long studentId;

        public AcceptStudent(@NotNull Long studentId) {
            this.studentId = studentId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class KickStudent {

        @NotNull
        private Long studentId;

        public KickStudent(@NotNull Long studentId) {
            this.studentId = studentId;
        }
    }
}
