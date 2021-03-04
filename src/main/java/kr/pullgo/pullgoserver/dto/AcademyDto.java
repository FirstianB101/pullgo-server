package kr.pullgo.pullgoserver.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface AcademyDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private String name;

        @NonNull
        private String phone;

        @NonNull
        private String address;

        @NonNull
        private Long ownerId;
    }

    @Data
    @Builder
    class Update {

        private String name;

        private String phone;

        private String address;

        private Long ownerId;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private String name;

        @NonNull
        private String phone;

        @NonNull
        private String address;

        @NonNull
        private Long ownerId;
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

        public KickStudent(Long studentId) {
            this.studentId = studentId;
        }
    }
}
