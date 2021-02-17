package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.persistence.model.Answer;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import kr.pullgo.pullgoserver.persistence.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttenderAnswerService {

    private final AttenderAnswerRepository attenderAnswerRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public AttenderAnswerService(
        AttenderAnswerRepository attenderAnswerRepository,
        QuestionRepository questionRepository) {
        this.attenderAnswerRepository = attenderAnswerRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public AttenderAnswerDto.Result createAttenderAnswer(AttenderAnswerDto.Create dto) {
        AttenderAnswer attenderAnswer = attenderAnswerRepository
            .save(AttenderAnswerDto.mapToEntity(dto));
        return AttenderAnswerDto.mapFromEntity(attenderAnswer);
    }

    @Transactional
    public AttenderAnswerDto.Result updateAttenderAnswer(Long id, AttenderAnswerDto.Update dto) {
        AttenderAnswer attenderAnswer = attenderAnswerRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderAnswer id was not found"));

        if (dto.getAnswer() != null) { attenderAnswer.setAnswer(new Answer(dto.getAnswer())); }
        attenderAnswer = attenderAnswerRepository.save(attenderAnswer);
        return AttenderAnswerDto.mapFromEntity(attenderAnswer);
    }

    @Transactional
    public void deleteAttenderAnswer(Long id) {
        int deleteResult = attenderAnswerRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderAnswer id was not found");
        }
    }

    @Transactional
    public AttenderAnswerDto.Result getAttenderAnswer(Long id) {
        AttenderAnswer attenderAnswer = attenderAnswerRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "AttenderAnswer id was not found"));
        return AttenderAnswerDto.mapFromEntity(attenderAnswer);
    }

    @Transactional
    public List<AttenderAnswerDto.Result> getAttenderAnswers() {
        List<AttenderAnswer> attenderAnswers = attenderAnswerRepository.findAll();
        return attenderAnswers.stream()
            .map(AttenderAnswerDto::mapFromEntity)
            .collect(Collectors.toList());
    }
}
