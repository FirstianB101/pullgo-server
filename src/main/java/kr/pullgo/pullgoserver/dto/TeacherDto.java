package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

public interface TeacherDto {

    @Data
    @Builder
    @NoArgsConstructor
    @With
    class Create {

        @NotNull
        @NotNull
        private AccountDto.Create account;

        public Create(@NotNull AccountDto.Create account) {
            this.account = account;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @With
    class Update {

        private AccountDto.Update account;

        public Update(AccountDto.Update account) {
            this.account = account;
        }
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private AccountDto.Result account;
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
