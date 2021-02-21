package kr.pullgo.pullgoserver.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface TeacherDto {

    @Data
    @Builder
    class Create {

        @NonNull
        private AccountDto.Create account;
    }

    @Data
    @Builder
    class Update {

        private AccountDto.Update account;
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
