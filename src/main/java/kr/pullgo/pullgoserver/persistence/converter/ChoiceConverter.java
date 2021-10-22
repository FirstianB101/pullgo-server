package kr.pullgo.pullgoserver.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import kr.pullgo.pullgoserver.error.exception.ChoiceJsonProcessingException;
import kr.pullgo.pullgoserver.persistence.model.MultipleChoice;

@Converter
public class ChoiceConverter implements AttributeConverter<MultipleChoice, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(MultipleChoice attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new ChoiceJsonProcessingException(e);
        }
    }

    @Override
    public MultipleChoice convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ChoiceJsonProcessingException(e);
        }
    }
}
