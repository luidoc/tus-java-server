package com.ejemplos.tus.server.termination;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestHandler;
import com.ejemplos.tus.server.RequestValidator;
import com.ejemplos.tus.server.util.AbstractTusExtension;

/**
 * This extension defines a way for the Client to terminate completed and unfinished
 * uploads allowing the Server to free up used resources.
 * <p>
 * If this extension is supported by the Server, it MUST be announced by adding "termination"
 * to the Tus-Extension header.
 */
public class TerminationExtension extends AbstractTusExtension {

    @Override
    public String getName() {
        return "termination";
    }

    @Override
    public Collection<HttpMethod> getMinimalSupportedHttpMethods() {
        return Arrays.asList(HttpMethod.OPTIONS, HttpMethod.DELETE);
    }

    @Override
    protected void initValidators(List<RequestValidator> requestValidators) {
        //All validation is all read done by the Core protocol
    }

    @Override
    protected void initRequestHandlers(List<RequestHandler> requestHandlers) {
        requestHandlers.add(new TerminationDeleteRequestHandler());
        requestHandlers.add(new TerminationOptionsRequestHandler());
    }
}
