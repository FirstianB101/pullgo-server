package kr.pullgo.pullgoserver.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface ClassroomDto {

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

        @NonNull
        private Long id;

        @NonNull
        private Long academyId;

        @NonNull
        private String name;
    }


    @Data
    @Builder
    @NoArgsConstructor
    class AcceptTeacher {

        @NonNull
        private Long teacherId;

        public AcceptTeacher(@NonNull Long teacherId) {
            this.teacherId = teacherId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class KickTeacher {

        @NonNull
        private Long teacherId;

        public KickTeacher(@NonNull Long teacherId) {
            this.teacherId = teacherId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class AcceptStudent {

        @NonNull
        private Long studentId;

        public AcceptStudent(@NonNull Long studentId) {
            this.studentId = studentId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class KickStudent {

        @NonNull
        private Long studentId;

        public KickStudent(@NonNull Long studentId) {
            this.studentId = studentId;
        }
    }
}
