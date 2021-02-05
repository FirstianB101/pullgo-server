package kr.pullgo.pullgoserver.dto;

import kr.pullgo.pullgoserver.persistence.model.Academy;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public interface AcademyDto {

    static Academy mapToEntity(AcademyDto.Create dto) {
        return Academy.builder()
            .name(dto.name)
            .phone(dto.phone)
            .address(dto.address)
            .build();
    }

    static AcademyDto.Result mapFromEntity(Academy academy) {
        return Result.builder()
            .name(academy.getName())
            .phone(academy.getPhone())
            .address(academy.getAddress())
            .build();
    }

    @Data
    @Builder
    class Create {

        @NonNull
        private String name;

        @NonNull
        private String phone;

        @NonNull
        private String address;
    }

    @Data
    @Builder
    class Update {

        private String name;

        private String phone;

        private String address;
    }

    @Data
    @Builder
    class Result {

        @NonNull
        private Long id;

        @NonNull
        private String name;

        @NonNull
        private String phone;

        @NonNull
        private String address;
    }
}
