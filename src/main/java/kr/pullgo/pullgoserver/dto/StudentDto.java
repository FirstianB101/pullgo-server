package kr.pullgo.pullgoserver.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Data;
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
    class ApplyAcademy {

        @NonNull
        private Long academyId;

        @JsonCreator
        public ApplyAcademy(@NonNull Long academyId) {
            this.academyId = academyId;
        }
    }

    @Data
    @Builder
    class RemoveAppliedAcademy {

        @NonNull
        private Long academyId;

        @JsonCreator
        public RemoveAppliedAcademy(@NonNull Long academyId) {
            this.academyId = academyId;
        }
    }

    @Data
    @Builder
    class ApplyClassroom {

        @NonNull
        private Long classroomId;

        @JsonCreator
        public ApplyClassroom(@NonNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }

    @Data
    @Builder
    class RemoveAppliedClassroom {

        @NonNull
        private Long classroomId;

        @JsonCreator
        public RemoveAppliedClassroom(@NonNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }
}
