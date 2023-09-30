package com.ejemplos.tus.server.creation;

import com.ejemplos.tus.server.util.AbstractExtensionRequestHandler;

/**
 * The Client and the Server SHOULD implement the upload creation extension.
 * If the Server supports this extension, it MUST add creation to the Tus-Extension header.
 * <p>
 * If the Server supports deferring length, it MUST add creation-defer-length to the Tus-Extension header.
 */
public class CreationOptionsRequestHandler extends AbstractExtensionRequestHandler {

    @Override
    protected void appendExtensions(StringBuilder extensionBuilder) {
        addExtension(extensionBuilder, "creation");
        addExtension(extensionBuilder, "creation-defer-length");
    }

}
