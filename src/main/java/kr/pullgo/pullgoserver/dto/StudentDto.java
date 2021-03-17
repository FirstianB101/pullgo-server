package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface StudentDto {

    @Data
    @Builder
    class Create {

        @NotNull
        private AccountDto.Create account;

        @NotNull
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

        @NotNull
        private Long id;

        @NotNull
        private AccountDto.Result account;

        @NotNull
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

        @NotNull
        private Long academyId;

        public ApplyAcademy(@NotNull Long academyId) {
            this.academyId = academyId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class RemoveAppliedAcademy {

        @NotNull
        private Long academyId;

        public RemoveAppliedAcademy(@NotNull Long academyId) {
            this.academyId = academyId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class ApplyClassroom {

        @NotNull
        private Long classroomId;

        public ApplyClassroom(@NotNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    class RemoveAppliedClassroom {

        @NotNull
        private Long classroomId;

        public RemoveAppliedClassroom(@NotNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }
}
