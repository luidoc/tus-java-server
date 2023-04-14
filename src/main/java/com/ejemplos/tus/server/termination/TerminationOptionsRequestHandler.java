package com.ejemplos.tus.server.termination;

import com.ejemplos.tus.server.util.AbstractExtensionRequestHandler;

/**
 * Add our download extension the Tus-Extension header
 */
public class TerminationOptionsRequestHandler extends AbstractExtensionRequestHandler {

    @Override
    protected void appendExtensions(StringBuilder extensionBuilder) {
        addExtension(extensionBuilder, "termination");
    }

}
