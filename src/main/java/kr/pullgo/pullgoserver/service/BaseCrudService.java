package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.mapper.DtoMapper;
import kr.pullgo.pullgoserver.persistence.repository.BaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service의 기본적인 CRUD 기능을 구현한 기반 클래스입니다.
 * <p>
 * 이 클래스는 Template Method Pattern을 기반으로 합니다. 따라서 Service를 구현하기 위해서는 이 클래스를 확장하여 {@link
 * #createOnDB(CREATE_DTO)}와 {@link #updateOnDB(ID, UPDATE_DTO)} 훅 메소드를 구현해주세요.
 *
 * @param <E>          Entity 타입
 * @param <ID>         Entity의 ID 타입
 * @param <CREATE_DTO> 리소스 생성을 위한 DTO 타입
 * @param <UPDATE_DTO> 리소스 수정을 위한 DTO 타입
 * @param <RESULT_DTO> 결과 전달을 위한 DTO 타입
 */
public abstract class BaseCrudService<E, ID, CREATE_DTO, UPDATE_DTO, RESULT_DTO> {

    private final Class<E> entityClass;
    private final DtoMapper<E, CREATE_DTO, RESULT_DTO> dtoMapper;
    private final BaseRepository<E, ID> repository;

    /**
     * 구현에 필요한 필드를 받는 생성자입니다.
     *
     * @param entityClass Entity의 클래스
     * @param repository  Entity의 Repository
     */
    public BaseCrudService(
        Class<E> entityClass,
        DtoMapper<E, CREATE_DTO, RESULT_DTO> dtoMapper,
        BaseRepository<E, ID> repository
    ) {
        this.entityClass = entityClass;
        this.dtoMapper = dtoMapper;
        this.repository = repository;
    }

    /**
     * 새로운 리소스를 생성합니다. 내부적으로 {@link #createOnDB(CREATE_DTO)}를 호출합니다.
     *
     * @param dto 생성할 리소스 정보가 담긴 DTO
     * @return 영속화한 Entity를 매핑한 DTO
     */
    @Transactional
    public RESULT_DTO create(CREATE_DTO dto) {
        E entity = createOnDB(dto);
        return dtoMapper.asResultDto(entity);
    }

    /**
     * DTO로부터 Entity를 생성하여 영속화합니다. DTO에 자식 리소스가 있으면 재귀적으로 영속화합니다.
     *
     * @param dto 생성할 리소스 정보가 담긴 DTO
     * @return 영속화한 리소스 Entity
     */
    abstract E createOnDB(CREATE_DTO dto);

    /**
     * 리소스를 읽어옵니다.
     *
     * @param id 읽어올 리소스의 ID
     * @return 읽어온 Entity를 매핑한 DTO
     */
    @Transactional
    public RESULT_DTO read(ID id) {
        E entity = repository.findById(id)
            .orElseThrow(this::notFoundException);
        return dtoMapper.asResultDto(entity);
    }

    /**
     * 리소스 목록을 읽어옵니다.
     *
     * @return 읽어온 Entity를 매핑한 DTO 목록
     */
    @Transactional
    public List<RESULT_DTO> search() {
        List<E> academies = repository.findAll();
        return academies.stream()
            .map(dtoMapper::asResultDto)
            .collect(Collectors.toList());
    }

    /**
     * 리소스를 수정합니다. 내부적으로 {@link #updateOnDB(E, UPDATE_DTO)}를 호출합니다.
     *
     * @param id  수정할 리소스의 ID
     * @param dto 수정할 정보가 담긴 DTO
     * @return 수정된 Entity를 매핑한 DTO
     */
    @Transactional
    public RESULT_DTO update(ID id, UPDATE_DTO dto) {
        E entity = repository.findById(id)
            .orElseThrow(this::notFoundException);
        entity = updateOnDB(entity, dto);
        return dtoMapper.asResultDto(entity);
    }

    /**
     * Entity를 수정 정보가 담긴 DTO와 병합합니다. DTO에 자식 리소스가 있으면 재귀적으로 병합합니다.
     *
     * @param entity 병합될 Entity
     * @param dto    수정할 정보가 담긴 DTO
     * @return 수정 정보와 병합된 리소스 Entity
     */
    abstract E updateOnDB(E entity, UPDATE_DTO dto);

    /**
     * 리소스를 삭제합니다.
     *
     * @param id 삭제할 리소스의 ID
     */
    @Transactional
    public void delete(ID id) {
        int cnt = repository.removeById(id);
        if (cnt == 0) {
            throw notFoundException();
        }
    }

    private ResponseStatusException notFoundException() {
        String reason = resourceName() + " id was not found";
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }

    private String resourceName() {
        return entityClass.getName();
    }
}
