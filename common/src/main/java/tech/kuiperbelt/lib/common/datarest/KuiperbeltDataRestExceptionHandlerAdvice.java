package tech.kuiperbelt.lib.common.datarest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.support.QueryMethodParameterConversionException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.kuiperbelt.lib.common.web.HttpErrorResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Too handler Exception thrown by Spring data rest framework
 */
@Slf4j
@RestControllerAdvice(basePackages = "org.springframework.data.rest.webmvc")
@Component
public class KuiperbeltDataRestExceptionHandlerAdvice {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse httpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = e.getMessage();
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code("http_message_not_readable")
                .message(message)
                .build();
        log.warn("异常： HttpMessageNotReadableException, 信息：{}， 返回:{}", message, result.toString());
        log.debug("异常： HttpMessageNotReadableException, 信息：{}， 返回:{}", message, result.toString() ,e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse transactionSystemException(TransactionSystemException e) {
        String message = e.getMessage();
        ConstraintViolationException violationException = findCause(e, ConstraintViolationException.class);
        if(violationException != null) {
            message = violationException.getConstraintViolations().stream().map(v -> v.getPropertyPath() + " " + v.getMessage()).collect(Collectors.joining("; "));
        }

        HttpErrorResponse.HttpErrorResponseBuilder builder = HttpErrorResponse.builder()
                .code("transaction_system")
                .message(message);
        if(violationException != null) {
            Map<String, String> errorDetails = violationException.getConstraintViolations().stream().collect(Collectors.toMap(
                    o -> String.valueOf(o.getPropertyPath()),
                    ConstraintViolation::getMessage));
            builder.detail(errorDetails);
        }
        HttpErrorResponse result = builder.build();
        log.warn("异常： TransactionSystemException, 信息：{}， 返回:{}", message, result.toString());
        log.debug("异常： TransactionSystemException, 信息：{}， 返回:{}", message, result.toString() ,e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse queryMethodParameterConversionException(QueryMethodParameterConversionException e) {
        String message = e.getMessage();
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code("query_method_parameter_conversion")
                .message(e.getMessage())
                .build();
        log.warn("异常： QueryMethodParameterConversionException, 信息：{}， 返回:{}", message, result.toString());
        log.debug("异常： QueryMethodParameterConversionException, 信息：{}， 返回:{}", message, result.toString() ,e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public HttpErrorResponse resourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        log.debug("异常： ResourceNotFoundException, 信息：{}， 返回: 空的Body", message ,e);
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code("resource_not_found")
                .message("Resource Not Found")
                .build();
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public HttpErrorResponse resourceNotFoundException(UnsupportedOperationException e) {
        String message = e.getMessage();
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code("unsupported_operation")
                .message(e.getMessage())
                .build();
        log.error("异常： UnsupportedOperationException, 信息：{}， 返回:{}", message, result.toString(), e);
        return result;
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<HttpErrorResponse> exception(DataIntegrityViolationException e) {
        String errorCode;
        String errorMessage = e.getMostSpecificCause().getMessage();
        HttpStatus status = errorMessage.contains("Duplicate entry")?
                HttpStatus.CONFLICT: HttpStatus.BAD_REQUEST;

        if(status == HttpStatus.CONFLICT) {
            errorCode = "duplicate_entry";
        } else if(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            errorCode = "constraint_violation";
        } else {
            errorCode = e.getMostSpecificCause().getClass().getSimpleName();
        }
        Map<String, String> details = Collections.singletonMap("detail", e.getMessage());
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(errorCode)
                .temporary(status == HttpStatus.CONFLICT)
                .message(errorMessage)
                .detail(details)
                .build();
        if(status == HttpStatus.CONFLICT) {
            log.warn("业务键重复(可重试): {}, 信息：{}， 返回:{}",
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    result.toString(),
                    e);
        } else {
            log.error("违法数据库约束: {}, 信息：{}， 返回:{}",
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    result.toString(),
                    e);
        }
        return ResponseEntity
                .status(status)
                .body(result);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse handleAccessDeniedException(RepositoryConstraintViolationException ex) {
        RepositoryConstraintViolationException nevEx =
                (RepositoryConstraintViolationException) ex;

        String errorCode = "repository_constraint_violation";
        String errorMessage = ex.getMessage();

        String detailMessage = nevEx.getErrors().getAllErrors().stream()
                .map(p -> p.toString()).collect(Collectors.joining("\n"));

        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(errorCode)
                .message(errorMessage)
                .detail(Collections.singletonMap("message", detailMessage))
                .build();
        log.warn("异常： RepositoryConstraintViolationException, 信息：{}， 返回:{}", ex.getMessage(), result.toString());
        log.debug("异常： RepositoryConstraintViolationException, 信息：{}， 返回:{}", ex.getMessage(), result.toString() , ex);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse exception(IllegalArgumentException e) {
        String errorCode = "bad_request";
        String errorMessage = e.getMessage();
        Map<String, String> details = Collections.emptyMap();
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(errorCode)
                .message(errorMessage)
                .detail(details)
                .build();
        log.warn("异常： IllegalArgumentException, 信息：{}， 返回:{}", e.getMessage(), result.toString());
        log.debug("异常： IllegalArgumentException, 信息：{}， 返回:{}", e.getMessage(), result.toString() ,e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public HttpErrorResponse exception(Exception e) {
        String errorCode = e.getClass().getSimpleName();
        String errorMessage = e.getMessage();
        Map<String, String> details = Collections.emptyMap();
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(errorCode)
                .message(errorMessage)
                .detail(details)
                .build();
        log.warn("异常： Exception, 信息：{}， 返回:{}", e.getMessage(), result.toString());
        log.debug("异常： Exception, 信息：{}， 返回:{}", e.getMessage(), result.toString() ,e);
        return result;
    }

    private <T extends Throwable> T findCause(Throwable exception, Class<T> tClass) {
        Throwable cause = exception.getCause();
        if(cause != null ) {
            if(tClass.isAssignableFrom(cause.getClass())) {
                //noinspection unchecked
                return (T)cause;
            } else {
                return findCause(cause, tClass);
            }
        } else {
            return null;
        }

    }
}
