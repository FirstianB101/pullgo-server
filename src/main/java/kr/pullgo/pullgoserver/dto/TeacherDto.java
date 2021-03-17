package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface TeacherDto {

    @Data
    @Builder
    @NoArgsConstructor
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
    class Update {

        private AccountDto.Update account;

        public Update(AccountDto.Update account) {
            this.account = account;
        }
    }

    @Data
    @Builder
    class Result {

        @NotNull
        private Long id;

        @NotNull
        private AccountDto.Result account;
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
