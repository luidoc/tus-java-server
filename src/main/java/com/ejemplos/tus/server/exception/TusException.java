package com.ejemplos.tus.server.exception;

import java.io.Serial;

/**
 * Super class for exception in the tus protocol
 */
public class TusException extends Exception {

    /**
	 *
	 */
	@Serial
    private static final long serialVersionUID = 1L;
	private final int status;

    public TusException(int status, String message) {
        this(status, message, null);
    }

    public TusException(int status, String message, Throwable e) {
        super(message, e);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
