package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.QuestionDto;
import kr.pullgo.pullgoserver.dto.mapper.QuestionDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestionService {

    private final QuestionDtoMapper dtoMapper;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionService(QuestionDtoMapper dtoMapper,
        QuestionRepository questionRepository) {
        this.dtoMapper = dtoMapper;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public QuestionDto.Result createQuestion(QuestionDto.Create dto) {
        Question question = questionRepository.save(dtoMapper.asEntity(dto));
        return dtoMapper.asResultDto(question);
    }

    @Transactional
    public QuestionDto.Result updateQuestion(Long id, QuestionDto.Update dto) {
        Question question = questionRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question id was not found"));

        if (dto.getContent() != null) { question.setContent(dto.getContent()); }
        if (dto.getPictureUrl() != null) { question.setPictureUrl(dto.getPictureUrl()); }
        if (dto.getAnswer() != null) { question.setAnswer(dto.getAnswer()); }

        question = questionRepository.save(question);
        return dtoMapper.asResultDto(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        int deleteResult = questionRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question id was not found");
        }
    }

    @Transactional
    public QuestionDto.Result getQuestion(Long id) {
        Question question = questionRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question id was not found"));
        return dtoMapper.asResultDto(question);
    }

    @Transactional
    public List<QuestionDto.Result> getQuestions() {
        List<Question> questions = questionRepository.findAll();
        return questions.stream()
            .map(dtoMapper::asResultDto)
            .collect(Collectors.toList());
    }
}
