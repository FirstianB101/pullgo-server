package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.mapper.AttenderAnswerDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.persistence.repository.AttenderStateRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttenderAnswerService extends
    BaseCrudService<AttenderAnswer, Long, AttenderAnswerDto.Create,
        AttenderAnswerDto.Update, AttenderAnswerDto.Result> {

    private final AttenderAnswerDtoMapper dtoMapper;
    private final AttenderAnswerRepository attenderAnswerRepository;
    private final QuestionRepository questionRepository;
    private final AttenderStateRepository attenderStateRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AttenderAnswerService(AttenderAnswerDtoMapper dtoMapper,
        AttenderAnswerRepository attenderAnswerRepository,
        QuestionRepository questionRepository,
        AttenderStateRepository attenderStateRepository,
        ServiceErrorHelper errorHelper) {
        super(AttenderAnswer.class, dtoMapper, attenderAnswerRepository);
        this.dtoMapper = dtoMapper;
        this.attenderAnswerRepository = attenderAnswerRepository;
        this.questionRepository = questionRepository;
        this.attenderStateRepository = attenderStateRepository;
        this.errorHelper = errorHelper;
    }

    @Override
    AttenderAnswer createOnDB(AttenderAnswerDto.Create dto) {
        AttenderAnswer attenderAnswer = dtoMapper.asEntity(dto);
        AttenderState attenderState = attenderStateRepository.findById(dto.getAttenderStateId())
            .orElseThrow(() -> errorHelper.notFound("AttenderState id was not found"));
        Question question = questionRepository.findById(dto.getQuestionId())
            .orElseThrow(() -> errorHelper.notFound("Question id was not found"));
        attenderState.addAnswer(attenderAnswer);
        attenderAnswer.setQuestion(question);
        return attenderAnswerRepository.save(attenderAnswer);
    }

    @Override
    AttenderAnswer updateOnDB(AttenderAnswer entity, AttenderAnswerDto.Update dto) {
        if (dto.getAnswer() != null) {
            entity.setAnswer(new Answer(dto.getAnswer()));
        }
        return attenderAnswerRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        return attenderAnswerRepository.removeById(id);
    }
}
