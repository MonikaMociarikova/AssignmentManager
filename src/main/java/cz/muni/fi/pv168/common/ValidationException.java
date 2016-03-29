package cz.muni.fi.pv168.common;

/**
 * Created by MONNY on 29-Mar-16.
 */
public class ValidationException extends RuntimeException {
    public ValidationException() {
    }

    public ValidationException(String msg) {
        super(msg);
    }
}
