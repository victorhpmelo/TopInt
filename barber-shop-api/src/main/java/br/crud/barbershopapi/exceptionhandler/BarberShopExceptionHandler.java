package br.crud.barbershopapi.exceptionhandler;

import br.crud.barbershopapi.controllers.response.ProblemResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Log4j2
@ControllerAdvice
public class BarberShopExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(
            final ResponseStatusException ex,
            final WebRequest request
    ) {
        final var status = ex.getStatusCode();
        final var response = new ProblemResponse(
                status.value(),
                OffsetDateTime.now(),
                ex.getReason()
        );
        return handleExceptionInternal(ex, response, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(
            final IllegalArgumentException ex,
            final WebRequest request
    ) {
        final var status = BAD_REQUEST;
        final var response = new ProblemResponse(
                status.value(),
                OffsetDateTime.now(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, response, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUncaught(final Exception ex, final WebRequest request){
        log.error("handleUncaught: ", ex);
        var status = INTERNAL_SERVER_ERROR;
        var response = new ProblemResponse(
                status.value(),
                OffsetDateTime.now(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, response, new HttpHeaders(), status, request);
    }

}