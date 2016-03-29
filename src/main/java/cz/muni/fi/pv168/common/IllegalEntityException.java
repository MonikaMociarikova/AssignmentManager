package cz.muni.fi.pv168.common;

/**
 * Created by MONNY on 29-Mar-16.
 */
public class IllegalEntityException extends RuntimeException {

    public IllegalEntityException() {
    }

    public IllegalEntityException(String msg) {
        super(msg);
    }

    public IllegalEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
