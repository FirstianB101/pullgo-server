package kr.pullgo.pullgoserver.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface TeacherDto {

    @Data
    @Builder
    @NoArgsConstructor
    class Create {

        @NonNull
        private AccountDto.Create account;

        public Create(@NonNull AccountDto.Create account) {
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

        @NonNull
        private Long id;

        @NonNull
        private AccountDto.Result account;
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
