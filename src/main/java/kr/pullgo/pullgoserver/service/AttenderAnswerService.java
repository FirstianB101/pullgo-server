package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.mapper.AttenderAnswerDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttenderAnswerService extends
    BaseCrudService<AttenderAnswer, Long, AttenderAnswerDto.Create,
        AttenderAnswerDto.Update, AttenderAnswerDto.Result> {

    private final AttenderAnswerDtoMapper dtoMapper;
    private final AttenderAnswerRepository attenderAnswerRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public AttenderAnswerService(AttenderAnswerDtoMapper dtoMapper,
        AttenderAnswerRepository attenderAnswerRepository,
        QuestionRepository questionRepository) {
        super(AttenderAnswer.class, dtoMapper, attenderAnswerRepository);
        this.dtoMapper = dtoMapper;
        this.attenderAnswerRepository = attenderAnswerRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    AttenderAnswer createOnDB(AttenderAnswerDto.Create dto) {
        return attenderAnswerRepository.save(dtoMapper.asEntity(dto));
    }

    @Override
    AttenderAnswer updateOnDB(AttenderAnswer entity, AttenderAnswerDto.Update dto) {
        if (dto.getAnswer() != null) {
            entity.setAnswer(new Answer(dto.getAnswer()));
        }
        return attenderAnswerRepository.save(entity);
    }
}
