package com.ejemplos.tus.server.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ejemplos.tus.server.util.AbstractTusExtension;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestHandler;
import com.ejemplos.tus.server.RequestValidator;
import com.ejemplos.tus.server.core.validation.ContentLengthValidator;
import com.ejemplos.tus.server.core.validation.ContentTypeValidator;
import com.ejemplos.tus.server.core.validation.HttpMethodValidator;
import com.ejemplos.tus.server.core.validation.IdExistsValidator;
import com.ejemplos.tus.server.core.validation.TusResumableValidator;
import com.ejemplos.tus.server.core.validation.UploadOffsetValidator;

/**
 * The core protocol describes how to resume an interrupted upload.
 * It assumes that you already have a URL for the upload, usually created via the Creation extension.
 * All Clients and Servers MUST implement the core protocol.
 */
public class CoreProtocol extends AbstractTusExtension {

    @Override
    public String getName() {
        return "core";
    }

    @Override
    public Collection<HttpMethod> getMinimalSupportedHttpMethods() {
        return Arrays.asList(HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.PATCH);
    }

    @Override
    protected void initValidators(List<RequestValidator> validators) {
        validators.add(new HttpMethodValidator());
        validators.add(new TusResumableValidator());
        validators.add(new IdExistsValidator());
        validators.add(new ContentTypeValidator());
        validators.add(new UploadOffsetValidator());
        validators.add(new ContentLengthValidator());
    }

    @Override
    protected void initRequestHandlers(List<RequestHandler> requestHandlers) {
        requestHandlers.add(new CoreDefaultResponseHeadersHandler());
        requestHandlers.add(new CoreHeadRequestHandler());
        requestHandlers.add(new CorePatchRequestHandler());
        requestHandlers.add(new CoreOptionsRequestHandler());
    }
}
