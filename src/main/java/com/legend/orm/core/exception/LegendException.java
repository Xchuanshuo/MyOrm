package com.legend.orm.core.exception;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description
 */
public class LegendException extends RuntimeException {

    private static long serialVersionUID = 0L;

    public LegendException() {
        super();
    }

    public LegendException(String message, Throwable cause) {
        super(message, cause);
    }

    public LegendException(String message) {
        super(message);
    }

    public LegendException(Throwable cause) {
        super(cause);
    }
}
