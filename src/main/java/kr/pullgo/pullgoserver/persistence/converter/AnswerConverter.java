package kr.pullgo.pullgoserver.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import kr.pullgo.pullgoserver.error.exception.AnswerJsonProcessingException;
import kr.pullgo.pullgoserver.persistence.entity.Answer;

@Converter
public class AnswerConverter implements AttributeConverter<Answer, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Answer attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new AnswerJsonProcessingException(e);
        }
    }

    @Override
    public Answer convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AnswerJsonProcessingException(e);
        }
    }
}
