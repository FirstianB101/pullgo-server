package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.AttenderAnswerDto;
import kr.pullgo.pullgoserver.dto.QuestionDto.Update;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.repository.AttenderAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttenderAnswerService {

    private final AttenderAnswerRepository attenderAnswerRepository;

    @Autowired
    public AttenderAnswerService(
        AttenderAnswerRepository attenderAnswerRepository) {
        this.attenderAnswerRepository = attenderAnswerRepository;
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
        Update dtoQuestion = dto.getQuestion();
        Question entityQuestion = attenderAnswer.getQuestion();
        if (dtoQuestion != null) {
            if (dtoQuestion.getAnswer() != null) {
                entityQuestion.setAnswer(dtoQuestion.getAnswer());
            }
            if (dtoQuestion.getContent() != null) {
                entityQuestion.setContent(dtoQuestion.getContent());
            }
            if (dtoQuestion.getPictureUrl() != null) {
                entityQuestion.setPictureUrl(dtoQuestion.getPictureUrl());
            }
        }
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
