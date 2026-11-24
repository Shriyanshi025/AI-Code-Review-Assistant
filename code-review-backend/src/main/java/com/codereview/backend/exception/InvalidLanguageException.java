package com.codereview.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLanguageException extends IllegalArgumentException {

    public InvalidLanguageException(String message) {
        super(message);
    }
}
