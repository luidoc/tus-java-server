package com.ejemplos.tus.server.upload.disk;

/**
 * Exception thrown when the disk storage path cannot be read or created.
 */
public class StoragePathNotAvailableException extends RuntimeException {
	
	private static final long serialVersionUID=1L;
	
    public StoragePathNotAvailableException(String message, Throwable e) {
        super(message, e);
    }
}
