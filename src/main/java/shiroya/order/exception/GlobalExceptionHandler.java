package shiroya.order.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {

        if (ex.getMessage().contains("Insufficient stock")) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }

        if (ex.getMessage().contains("Product not found")) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }

        return ResponseEntity.status(500).body("Something went wrong");
    }
}
