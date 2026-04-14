package shiroya.order.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UnAuthorizedUserException.class)
    public ResponseEntity<String> unAuthorizedUserException(RuntimeException ex)
    {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> insufficientStockException(RuntimeException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> productNotFoundException(RuntimeException ex) {

        return ResponseEntity.status(404).body(ex.getMessage());

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> userNotFoundException(RuntimeException ex) {

        return ResponseEntity.status(400).body(ex.getMessage());

    }
}
