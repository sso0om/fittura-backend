package com.fittura.global.exceptionhandler;

import com.fittura.global.error.CommonErrorCode;
import com.fittura.global.error.ErrorCode;
import com.fittura.global.error.ValidationError;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.rsdata.RsData;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException e) {
        return new ResponseEntity<>(
                RsData.error(
                        CommonErrorCode.NOT_FOUND
                ),
                NOT_FOUND
        );
    }

    // @RequestParam 또는 @PathVariable 등 개별 파라미터 유효성 검사 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public  ResponseEntity<RsData<List<ValidationError>>> handle(ConstraintViolationException e) {
        List<ValidationError> validationErrors = e.getConstraintViolations()
                .stream()
                .map(ValidationError::from)
                .toList();

        return new ResponseEntity<>(
                RsData.error(
                        CommonErrorCode.VALIDATION_ERROR,
                        validationErrors
                ),
                BAD_REQUEST
        );
    }

    // @RequestBody 유효성 검사 실패 (dto)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<List<ValidationError>>> handle(MethodArgumentNotValidException e) {
        List<ValidationError> validationErrors = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return ValidationError.from(fieldError);
                    } else {
                        return new ValidationError(error.getObjectName(), error.getDefaultMessage());
                    }
                })
                .toList();

        return new ResponseEntity<>(
                RsData.error(
                        CommonErrorCode.VALIDATION_ERROR,
                        validationErrors
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(
                RsData.error(
                        CommonErrorCode.BAD_REQUEST
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handle(ServiceException e) {
        ErrorCode errorCode = e.getErrorCode();

        return new ResponseEntity<>(
                RsData.error(
                        errorCode
                ),
                errorCode.httpStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Map<String, String>>> handle(Exception e) {
        String traceId = UUID.randomUUID().toString();

        // traceId를 포함하여 에러 로그 상세히 기록
        log.error("Unhandled server error occurred. traceId={}", traceId, e);

        // 클라이언트에게 traceId와 간단한 메시지만 전달
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(
                RsData.error(
                        errorCode,
                        Map.of("traceId", traceId)
                ),
                errorCode.httpStatus()
        );
    }
}
