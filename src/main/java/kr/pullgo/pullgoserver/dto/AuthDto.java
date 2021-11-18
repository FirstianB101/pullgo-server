package kr.pullgo.pullgoserver.dto;

import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

public interface AuthDto {

    @Data
    @Builder
    class GenerateToken {

        @NotEmpty
        private String username;

        @NotEmpty
        private String password;

    }

    @Data
    @Builder
    class GenerateTokenResult {

        @NotEmpty
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
