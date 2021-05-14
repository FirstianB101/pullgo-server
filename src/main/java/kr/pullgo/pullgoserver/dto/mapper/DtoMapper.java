package kr.pullgo.pullgoserver.dto.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.util.Streamable;

public interface DtoMapper<E, DTO_CREATE, DTO_RESULT> {

    E asEntity(DTO_CREATE dto);

    DTO_RESULT asResultDto(E entity);

    default List<DTO_RESULT> asResultDto(Collection<E> entities) {
        return asResultDto(Streamable.of(entities));
    }

    default List<DTO_RESULT> asResultDto(Streamable<E> entities) {
        return entities.stream()
            .map(this::asResultDto)
            .collect(Collectors.toList());
    }

}
