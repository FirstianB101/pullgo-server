package kr.pullgo.pullgoserver.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class AnswerJsonProcessingException extends RuntimeException {

    public AnswerJsonProcessingException(Throwable throwable) {
        super(throwable);
    }
}
