package kr.pullgo.pullgoserver.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
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
    }

    @Data
    @Builder
    class Update {

        private String name;

        private String phone;

        private String address;
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
        public KickStudent(Long studentId) {
            this.studentId = studentId;
        }
    }
}
