package dockmind.demo.exception;

import dockmind.demo.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ResourceNotFound.class)
        public ResponseEntity<?> handleNotFound(ResourceNotFound ex) {
                logger.warn("resource not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message(ex.getMessage())
                                .data(null)
                                .build());
        }

        @ExceptionHandler(DocumentProcessingException.class)
        public ResponseEntity<?> handleNotFound(DocumentProcessingException ex) {
                logger.warn("failed to process document:  {} ", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("failed to process document" + ex.getMessage())
                                .data(null)
                                .build());
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiResponse<Object>> handleMaxSize(MaxUploadSizeExceededException ex) {
                logger.warn("File size limit exceeded: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(ApiResponse.builder().success(false).message("File size exceeds the allowed limit (25MB)").build());


        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleValidationErrors(
                MethodArgumentNotValidException ex) {

                logger.warn("Validation failed: {}", ex.getMessage());

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.builder()
                                        .success(false)
                                        .message("Validation failed")
                                        .data(null)
                                        .build()
                        );
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
                logger.warn("Illegal argument: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.builder()
                                        .success(false)
                                        .message(ex.getMessage())
                                        .data(null)
                                        .build()
                        );
        }
}