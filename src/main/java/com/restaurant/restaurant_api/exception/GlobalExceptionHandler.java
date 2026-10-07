package com.restaurant.restaurant_api.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ConstraintViolationException;

import com.restaurant.restaurant_api.dto.response.ErrorResponse;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(TokenInvalidoException.class)
        public ResponseEntity<ErrorResponse> handleTokenInvalido(
                        TokenInvalidoException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.UNAUTHORIZED,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(UsuarioNoEncontradoException.class)
        public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(
                        UsuarioNoEncontradoException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(UsuarioBloqueadoException.class)
        public ResponseEntity<ErrorResponse> handleUsuarioBloqueado(
                        UsuarioBloqueadoException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.FORBIDDEN,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(FirebaseNoDisponibleException.class)
        public ResponseEntity<ErrorResponse> handleFirebaseNoDisponible(
                        FirebaseNoDisponibleException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                ex.getMessage(),
                                request);
        }

        // NUEVO: captura errores de integridad de la base de datos.
        // Ejemplo: intentar insertar un firebase_uid o email que ya existe.
        // Se responde 409 CONFLICT porque la operación entra en conflicto
        // con datos que ya existen en la base de datos.
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.CONFLICT,
                                "La operación entra en conflicto con los datos existentes",
                                request);
        }

        // NUEVO: captura errores producidos por @Valid cuando los datos
        // recibidos no cumplen las validaciones del DTO.
        // Ejemplo: @NotBlank, @NotNull, @Email, @Size, etc.
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Los datos enviados no son válidos",
                                request);
        }

        // NUEVO: captura errores cuando Spring no puede leer el body
        // de la petición.
        // Ejemplo: JSON mal formado o un tipo de dato incompatible.
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "El formato de la solicitud no es válido",
                                request);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResourceFound(
                        NoResourceFoundException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                "El recurso solicitado no existe",
                                request);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.METHOD_NOT_ALLOWED,
                                "El método HTTP utilizado no está permitido para este recurso",
                                request);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
                        HttpMediaTypeNotSupportedException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                "El tipo de contenido enviado no es compatible",
                                request);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
                        MissingServletRequestParameterException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Falta un parámetro requerido en la solicitud",
                                request);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Uno de los parámetros enviados no tiene un formato válido",
                                request);
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
                        HandlerMethodValidationException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Los parámetros enviados no son válidos",
                                request);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Los datos enviados no son válidos",
                                request);
        }

        @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(
                        HttpMediaTypeNotAcceptableException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.NOT_ACCEPTABLE,
                                "El formato de respuesta solicitado no es compatible",
                                request);
        }

        // NUEVO: catch-all.
        // Si ocurre una excepción que ninguno de los handlers anteriores
        // contempla, llega acá.
        //
        // IMPORTANTE:
        // No devolvemos ex.getMessage(), porque podría exponer información
        // técnica interna de la aplicación o de la base de datos.
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Error interno en {}", request.getRequestURI(), ex);

                return buildResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocurrió un error interno",
                                request);
        }

        // NUEVO: método común para construir siempre el mismo ErrorResponse.
        //
        // Antes repetías este bloque en cada handler:
        //
        // new ErrorResponse(...)
        //
        // Ahora todos los handlers reutilizan este método.
        private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                OffsetDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                request.getRequestURI());

                return ResponseEntity
                                .status(status)
                                .body(errorResponse);
        }
}