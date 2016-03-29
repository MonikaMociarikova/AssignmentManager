package cz.muni.fi.pv168.common;

/**
 * Created by MM on 14-Mar-16.
 */
public class ServiceFailureException extends RuntimeException{

    public ServiceFailureException(String msg) {
        super(msg);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }


}
