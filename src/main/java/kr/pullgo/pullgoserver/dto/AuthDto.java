package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public interface AuthDto {

    @Data
    @Builder
    class GenerateToken {

        @NotNull
        private String username;

        @NotNull
        private String password;

    }

    @Data
    @Builder
    class GenerateTokenResult {

        @NotNull
        private String token;

        private StudentDto.Result student;

        private TeacherDto.Result teacher;

    }

    @Data
    @Builder
    class MeResult {

        private StudentDto.Result student;

        private TeacherDto.Result teacher;

    }

}
