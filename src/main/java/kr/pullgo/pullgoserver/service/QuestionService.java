package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.mapper.QuestionDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService extends
    BaseCrudService<Question, Long, QuestionDto.Create, QuestionDto.Update, QuestionDto.Result> {

    private final QuestionDtoMapper dtoMapper;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionService(QuestionDtoMapper dtoMapper,
        QuestionRepository questionRepository) {
        super(Question.class, dtoMapper, questionRepository);
        this.dtoMapper = dtoMapper;
        this.questionRepository = questionRepository;
    }

    @Override
    Question createOnDB(QuestionDto.Create dto) {
        return questionRepository.save(dtoMapper.asEntity(dto));
    }

    @Override
    Question updateOnDB(Question entity, QuestionDto.Update dto) {
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
        if (dto.getPictureUrl() != null) {
            entity.setPictureUrl(dto.getPictureUrl());
        }
        if (dto.getAnswer() != null) {
            entity.setAnswer(dto.getAnswer());
        }
        return questionRepository.save(entity);
    }
}
