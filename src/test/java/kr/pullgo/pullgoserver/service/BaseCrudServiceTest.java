package kr.pullgo.pullgoserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.Optional;
import kr.pullgo.pullgoserver.dto.mapper.DtoMapper;
import kr.pullgo.pullgoserver.persistence.repository.BaseRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BaseCrudServiceTest {

    private BaseCrudService<Dummy, Long, DummyDto.Create, DummyDto.Update, DummyDto.Result> service;

    @Mock
    private BaseRepository<Dummy, Long> repository;

    @Mock
    private DtoMapper<Dummy, DummyDto.Create, DummyDto.Result> dtoMapper;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        service = mock(BaseCrudService.class, withSettings()
            .useConstructor(Dummy.class, dtoMapper, repository)
            .defaultAnswer(CALLS_REAL_METHODS));
    }

    @Test
    void create() {
        // When
        service.create(dummyCreateDto());

        // Then
        verify(service).createOnDB(any(DummyDto.Create.class));
    }

    @Test
    void read() {
        // Given
        given(repository.findById(0L))
            .willReturn(Optional.of(dummyEntity()));

        // When
        service.read(0L);

        // Then
        verify(dtoMapper).asResultDto(any(Dummy.class));
    }

    @Test
    void read_ResourceNotFound_ExceptionThrown() {
        // Given
        given(repository.findById(0L))
            .willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> service.read(0L));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Dummy id was not found");
    }

    @Test
    void search() {
        // Given
        given(repository.findAll(any(Pageable.class)))
            .willReturn(Page.empty());

        // When
        service.search(Pageable.unpaged());

        // Then
        verify(repository).findAll(any(Pageable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchBySpec() {
        // Given
        given(repository.findAll(any(Specification.class), any(Pageable.class)))
            .willReturn(Page.empty());

        // When
        service.search(Specification.where(null), Pageable.unpaged());

        // Then
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void update() {
        // Given
        given(repository.findById(0L))
            .willReturn(Optional.of(dummyEntity()));

        // When
        service.update(0L, dummyUpdateDto());

        // Then
        verify(service).updateOnDB(any(Dummy.class), any(DummyDto.Update.class));
    }

    @Test
    void update_ResourceNotFound_ExceptionThrown() {
        // Given
        given(repository.findById(0L))
            .willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> service.update(0L, dummyUpdateDto()));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Dummy id was not found");
    }

    @Test
    void delete() {
        // Given
        given(repository.removeById(0L))
            .willReturn(1);

        // When
        service.delete(0L);

        // Then
        verify(repository).removeById(0L);
    }

    @Test
    void delete_ResourceNotFound_ExceptionThrown() {
        // Given
        given(repository.removeById(0L))
            .willReturn(0);

        // When
        Throwable thrown = catchThrowable(() -> service.delete(0L));

        // Then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Dummy id was not found");
    }

    private Dummy dummyEntity() {
        return new Dummy(0L, "bar");
    }

    private DummyDto.Create dummyCreateDto() {
        return new DummyDto.Create(0L, "bar");
    }

    private DummyDto.Update dummyUpdateDto() {
        return new DummyDto.Update();
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    private static class Dummy {

        private Long id;
        private String foo;
    }

    private interface DummyDto {

        @Data
        class Create {

            @NonNull
            private final Long id;

            @NonNull
            private final String foo;
        }

        @Data
        class Update {

            private String bar;
        }

        @Data
        class Result {

            @NonNull
            private final Long id;

            @NonNull
            private final String foo;
        }
    }
}