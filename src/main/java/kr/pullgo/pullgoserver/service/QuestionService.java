package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.mapper.QuestionDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final QuestionDtoMapper dtoMapper;
    private final QuestionRepository questionRepository;
    private final RepositoryHelper repoHelper;

    @Autowired
    public QuestionService(
        QuestionDtoMapper dtoMapper,
        QuestionRepository questionRepository,
        RepositoryHelper repoHelper) {
        this.dtoMapper = dtoMapper;
        this.questionRepository = questionRepository;
        this.repoHelper = repoHelper;
    }

    @Transactional
    public QuestionDto.Result create(QuestionDto.Create dto) {
        Question question = dtoMapper.asEntity(dto);

        Exam exam = repoHelper.findExamOrThrow(dto.getExamId());
        exam.addQuestion(question);

        return dtoMapper.asResultDto(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public QuestionDto.Result read(Long id) {
        Question entity = repoHelper.findQuestionOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto.Result> search(Specification<Question> spec, Pageable pageable) {
        Page<Question> entities = questionRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public QuestionDto.Result update(Long id, QuestionDto.Update dto) {
        Question entity = repoHelper.findQuestionOrThrow(id);
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
        if (dto.getPictureUrl() != null) {
            entity.setPictureUrl(dto.getPictureUrl());
        }
        if (dto.getAnswer() != null) {
            entity.setAnswer(new Answer(dto.getAnswer()));
        }
        return dtoMapper.asResultDto(questionRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Question entity = repoHelper.findQuestionOrThrow(id);
        questionRepository.delete(entity);
    }
}
