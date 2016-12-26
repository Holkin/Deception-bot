package deception;

/**
 * Created by Arkitekt on 12/17/2016.
 */
public class IllegalMoveException extends RuntimeException {
    public IllegalMoveException () {

    }

    public IllegalMoveException(String message) {
        super(message);
    }
}
