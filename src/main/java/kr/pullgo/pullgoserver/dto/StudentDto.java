package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

public interface StudentDto {

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        private AccountDto.Create account;

        @NotEmpty
        private String parentPhone;

        @NotEmpty
        private String schoolName;

        @NotNull
        private Integer schoolYear;
    }

    @Data
    @Builder
    @With
    class Update {

        private AccountDto.Update account;

        private String parentPhone;

        private String schoolName;

        private Integer schoolYear;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private AccountDto.Result account;

        @NotEmpty
        private String parentPhone;

        @NotEmpty
        private String schoolName;

        @NotNull
        private Integer schoolYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @With
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
    @With
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
    @With
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
    @With
    class RemoveAppliedClassroom {

        @NotNull
        private Long classroomId;

        public RemoveAppliedClassroom(@NotNull Long classroomId) {
            this.classroomId = classroomId;
        }
    }
}
