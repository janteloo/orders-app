package com.geocom.orders.controller.error;

import com.geocom.orders.exception.BadRequestException;
import com.geocom.orders.exception.ResourceNotFoundException;
import com.geocom.orders.exception.EntityAlreadyExistException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class CustomResponseHandler {

    private final Logger logger = Logger.getLogger(CustomResponseHandler.class);

    private static final String EMPTY_SPACE = " ";

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleUnknownException(Exception ex, WebRequest request) {
        logger.error("Internal error found", ex);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public final ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found error", ex);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            EntityAlreadyExistException.class,
            BadRequestException.class
    })
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public final ResponseEntity<ErrorDetails> handleEntityAlreadyExist(Exception ex, WebRequest request) {
        logger.error("Entity already exists error", ex);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public final ResponseEntity<ErrorDetails> handleInvalidRequest(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("An invalid argument was found", ex);

        BindingResult result = ex.getBindingResult();
        FieldError field = result.getFieldError();
        StringBuilder builder = new StringBuilder();
        String errorMessage = builder.append(field.getField())
                                    .append(EMPTY_SPACE)
                                    .append(field.getDefaultMessage()).toString();

        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorMessage, request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}
