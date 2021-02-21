package kr.pullgo.pullgoserver.dto.mapper;

public interface DtoMapper<E, DTO_CREATE, DTO_RESULT> {

    E asEntity(DTO_CREATE dto);

    DTO_RESULT asResultDto(E entity);
}
