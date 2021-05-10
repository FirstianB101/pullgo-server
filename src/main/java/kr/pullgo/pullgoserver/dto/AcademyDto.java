package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

public interface AcademyDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private String name;

        @NotNull
        private String phone;

        @NotNull
        private String address;

        @NotNull
        private Long ownerId;
    }

    @Data
    @Builder
    @With
    class Update {

        private String name;

        private String phone;

        private String address;

        private Long ownerId;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private String name;

        @NotNull
        private String phone;

        @NotNull
        private String address;

        @NotNull
        private Long ownerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @With
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
    @With
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
    @With
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
    @With
    class KickStudent {

        @NotNull
        private Long studentId;

        public KickStudent(Long studentId) {
            this.studentId = studentId;
        }
    }
}
