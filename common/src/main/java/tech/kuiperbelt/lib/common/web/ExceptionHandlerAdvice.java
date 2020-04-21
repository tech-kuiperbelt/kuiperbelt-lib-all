package tech.kuiperbelt.lib.common.web;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * exception handler
 */
@Slf4j
@RestControllerAdvice(basePackages = "tech.kuiperbelt.volcano")
@Component("tech.kuiperbelt.volcano.common.web.ExceptionHandlerAdvice")
public class ExceptionHandlerAdvice {

    @Value("${spring.application.name:未知服务}")
    private String svc;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse illegalArgumentExceptionHandler(IllegalArgumentException e) {
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(svc + "_" + "illegal_argument_exception")
                .message(e.getMessage())
                .build();
        log.warn("异常： IllegalArgumentException, 信息：{}， 返回:{}", e.getMessage(), result.toString());
        log.debug("异常： IllegalArgumentException, 信息：{}， 返回:{}", e.getMessage(), result.toString(), e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String errorMessage = allErrors.stream().map(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                return fieldError.getField() + ":" + fieldError.getDefaultMessage();
            } else {
                return error.getDefaultMessage();
            }
        }).collect(Collectors.joining(";"));

        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(svc + "_" + "method_argument_not_valid_exception")
                .message(errorMessage)
                .build();
        log.warn("异常： MethodArgumentNotValidException, 信息：{}， 返回:{}", e.getMessage(), result.toString());
        log.debug("异常： MethodArgumentNotValidException, 信息：{}， 返回:{}", e.getMessage(), result.toString(), e);
        return result;
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse methodArgumentNotValidException(JsonMappingException e) {

        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(svc + "_" + "json_mapping_exception")
                .message(e.getMessage())
                .build();
        log.warn("异常： JsonMappingException, 信息：{}， 返回:{}", e.getMessage(), result.toString());
        log.debug("异常： JsonMappingException, 信息：{}， 返回:{}", e.getMessage(), result.toString(), e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HttpErrorResponse methodArgumentNotValidException(HttpMessageNotReadableException e) {

        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(svc + "_" + "http_message_not_readable_exception")
                .message(e.getMessage())
                .build();
        log.warn("异常： HttpMessageNotReadableException, 信息：{}， 返回:{}", e.getMessage(), result.toString());
        log.debug("异常： HttpMessageNotReadableException, 信息：{}， 返回:{}", e.getMessage(), result.toString(), e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public HttpErrorResponse runtimeException(RuntimeException e) {
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(svc + "_" + "runtime_exception")
                .message(e.getMessage())
                .build();
        log.error("异常： RuntimeException, 信息：{}， 返回:{}", e.getMessage(), result.toString(), e);
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public HttpErrorResponse exception(Exception e) {
        HttpErrorResponse result = HttpErrorResponse.builder()
                .code(svc + "_" + "_exception")
                .message(e.getMessage())
                .build();
        log.error("异常： Exception, 信息：{}， 返回:{}", e.getMessage(), result.toString(), e);
        return result;
    }
}
