package shiroya.order.exception;

public class UnAuthorizedUserException extends RuntimeException {

    public UnAuthorizedUserException(String message){
        super(message);
    }
}
