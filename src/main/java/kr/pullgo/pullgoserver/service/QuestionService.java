package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.mapper.QuestionDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService extends
    BaseCrudService<Question, Long, QuestionDto.Create, QuestionDto.Update, QuestionDto.Result> {

    private final QuestionDtoMapper dtoMapper;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public QuestionService(
        QuestionDtoMapper dtoMapper,
        QuestionRepository questionRepository,
        ExamRepository examRepository,
        ServiceErrorHelper errorHelper) {
        super(Question.class, dtoMapper, questionRepository);
        this.dtoMapper = dtoMapper;
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.errorHelper = errorHelper;
    }

    @Override
    Question createOnDB(QuestionDto.Create dto) {
        Question question = dtoMapper.asEntity(dto);
        Exam exam = examRepository.findById(dto.getExamId())
            .orElseThrow(() -> errorHelper.notFound("Exam id was not found"));
        exam.addQuestion(question);
        return questionRepository.save(question);
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
            entity.setAnswer(new Answer(dto.getAnswer()));
        }
        return questionRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        return questionRepository.removeById(id);
    }
}
