package com.ejemplos.tus.server.expiration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ejemplos.tus.server.util.AbstractTusExtension;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestHandler;
import com.ejemplos.tus.server.RequestValidator;

/**
 * The Server MAY remove unfinished uploads once they expire.
 */
public class ExpirationExtension extends AbstractTusExtension {

    @Override
    public String getName() {
        return "expiration";
    }

    @Override
    public Collection<HttpMethod> getMinimalSupportedHttpMethods() {
        return Arrays.asList(HttpMethod.OPTIONS, HttpMethod.PATCH, HttpMethod.POST);
    }

    @Override
    protected void initValidators(List<RequestValidator> requestValidators) {
        //No validators
    }

    @Override
    protected void initRequestHandlers(List<RequestHandler> requestHandlers) {
        requestHandlers.add(new ExpirationOptionsRequestHandler());
        requestHandlers.add(new ExpirationRequestHandler());
    }
}
