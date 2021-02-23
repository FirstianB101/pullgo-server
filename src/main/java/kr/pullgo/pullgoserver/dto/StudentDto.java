package kr.pullgo.pullgoserver.dto;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface StudentDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private AccountDto.Create account;

        @NonNull
        private String parentPhone;

        @NotNull
        private String schoolName;

        @NotNull
        private Integer schoolYear;
    }

    @Data
    @Builder
    class Update {

        private AccountDto.Update account;

        private String parentPhone;

        private String schoolName;

        private Integer schoolYear;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private AccountDto.Result account;

        @NonNull
        private String parentPhone;

        @NotNull
        private String schoolName;

        @NotNull
        private Integer schoolYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    class ApplyAcademy {

        @NonNull
        private Long academyId;

        public ApplyAcademy(@NonNull Long academyId) {
            this.academyId = academyId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class RemoveAppliedAcademy {

        @NonNull
        private Long academyId;

        public RemoveAppliedAcademy(@NonNull Long academyId) {
            this.academyId = academyId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class ApplyClassroom {

        @NonNull
        private Long classroomId;

        public ApplyClassroom(@NonNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class RemoveAppliedClassroom {

        @NonNull
        private Long classroomId;

        public RemoveAppliedClassroom(@NonNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }
}
