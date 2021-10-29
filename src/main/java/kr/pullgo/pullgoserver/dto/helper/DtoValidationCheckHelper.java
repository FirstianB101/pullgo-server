package kr.pullgo.pullgoserver.dto.helper;

import java.util.Collection;
import java.util.Map;
import kr.pullgo.pullgoserver.dto.QuestionDto;

public class DtoValidationCheckHelper {

    private static boolean hasNullOrEmpty(Object... datas) {
        for (var data : datas) {
            if (data instanceof Collection<?>) {
                if (((Collection<?>) data).isEmpty()) {
                    return true;
                } else {
                    for (var single : (Collection<?>) data) {
                        if (hasNullOrEmpty(single))
                            return true;
                    }
                }
            } else if (data instanceof Map<?, ?>) {
                if (((Map<?, ?>) data).isEmpty()) {
                    return true;
                }
            } else if (data instanceof QuestionDto.QuestionConfig) {
                return !isValid((QuestionDto.QuestionConfig) data);
            } else {
                if (data == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValid(QuestionDto.QuestionConfig dto) {
        return !hasNullOrEmpty(dto.getContent(), dto.getAnswer(), dto.getChoice());
    }

    public static boolean isValid(QuestionDto.MultipleCreate dto) {
        return !hasNullOrEmpty(dto.getExamId(), dto.getQuestionConfigs());
    }

    public static boolean isValid(QuestionDto.Create dto) {
        return !hasNullOrEmpty(dto.getExamId(), dto.getQuestionConfig());
    }
}
