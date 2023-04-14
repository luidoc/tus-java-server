package com.ejemplos.tus.server.download;

import com.ejemplos.tus.server.util.AbstractExtensionRequestHandler;

/**
 * Add our download extension the Tus-Extension header
 */
public class DownloadOptionsRequestHandler extends AbstractExtensionRequestHandler {

    @Override
    protected void appendExtensions(StringBuilder extensionBuilder) {
        addExtension(extensionBuilder, "download");
    }

}
