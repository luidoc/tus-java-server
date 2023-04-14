package com.ejemplos.tus.server.util;

import com.ejemplos.tus.server.RequestHandler;

/**
 * Abstract {@link RequestHandler} implementation that contains the common functionality
 */
public abstract class AbstractRequestHandler implements RequestHandler {

    @Override
    public boolean isErrorHandler() {
        return false;
    }

}
