package com.ejemplos.tus.server.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Exception thrown when the client sends a request for a checksum algorithm we do not support
 */
public class ChecksumAlgorithmNotSupportedException extends TusException {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ChecksumAlgorithmNotSupportedException(String message) {
        super(HttpServletResponse.SC_BAD_REQUEST, message);
    }
}
