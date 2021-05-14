package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.mapper.AttenderAnswerDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttenderAnswerService {

    private final AttenderAnswerDtoMapper dtoMapper;
    private final AttenderAnswerRepository attenderAnswerRepository;
    private final RepositoryHelper repoHelper;

    @Autowired
    public AttenderAnswerService(AttenderAnswerDtoMapper dtoMapper,
        AttenderAnswerRepository attenderAnswerRepository,
        RepositoryHelper repoHelper) {
        this.dtoMapper = dtoMapper;
        this.attenderAnswerRepository = attenderAnswerRepository;
        this.repoHelper = repoHelper;
    }

    @Transactional
    public AttenderAnswerDto.Result create(AttenderAnswerDto.Create dto) {
        AttenderAnswer attenderAnswer = dtoMapper.asEntity(dto);

        Question question = repoHelper.findQuestionOrThrow(dto.getQuestionId());
        attenderAnswer.setQuestion(question);

        AttenderState attenderState = repoHelper.findAttenderStateOrThrow(dto.getAttenderStateId());
        attenderState.addAnswer(attenderAnswer);

        return dtoMapper.asResultDto(attenderAnswerRepository.save(attenderAnswer));
    }

    @Transactional(readOnly = true)
    public AttenderAnswerDto.Result read(Long id) {
        AttenderAnswer entity = repoHelper.findAttenderAnswerOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AttenderAnswerDto.Result> search(Specification<AttenderAnswer> spec,
        Pageable pageable) {
        Page<AttenderAnswer> entities = attenderAnswerRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public AttenderAnswerDto.Result update(Long id, AttenderAnswerDto.Update dto) {
        AttenderAnswer entity = repoHelper.findAttenderAnswerOrThrow(id);
        if (dto.getAnswer() != null) {
            entity.setAnswer(new Answer(dto.getAnswer()));
        }
        return dtoMapper.asResultDto(attenderAnswerRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        AttenderAnswer entity = repoHelper.findAttenderAnswerOrThrow(id);
        attenderAnswerRepository.delete(entity);
    }
}
