package com.example.user.exception;

import com.example.user.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IncorrectPasswordException.class})
    public ResponseEntity<Object> handleIncorrectPasswordException(IncorrectPasswordException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
    @ExceptionHandler({PhoneNumberAlreadyUsedException.class})
    public ResponseEntity<Object> handlePhoneNumberAlreadyUsedException(PhoneNumberAlreadyUsedException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
    @ExceptionHandler({PhoneNumberNotUsedException.class})
    public ResponseEntity<Object> handlePhoneNumberNotUsedException(PhoneNumberNotUsedException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(exception.getMessage()));
    }
    @ExceptionHandler({UsernameAlreadyUsedException.class})
    public ResponseEntity<Object> handleUsernameAlreadyUsedException(UsernameAlreadyUsedException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Server Error"));
    }
    @ExceptionHandler({NotEnoughFundsException.class})
    public ResponseEntity<Object> handleNotEnoughFundsException(NotEnoughFundsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
