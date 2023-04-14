package com.ejemplos.tus.server.creation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ejemplos.tus.server.creation.validation.PostEmptyRequestValidator;
import com.ejemplos.tus.server.creation.validation.PostURIValidator;
import com.ejemplos.tus.server.creation.validation.UploadDeferLengthValidator;
import com.ejemplos.tus.server.creation.validation.UploadLengthValidator;
import com.ejemplos.tus.server.util.AbstractTusExtension;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestHandler;
import com.ejemplos.tus.server.RequestValidator;

/**
 * The Client and the Server SHOULD implement the upload creation extension. If the Server supports this extension.
 */
public class CreationExtension extends AbstractTusExtension {

    @Override
    public String getName() {
        return "creation";
    }

    @Override
    public Collection<HttpMethod> getMinimalSupportedHttpMethods() {
        return Arrays.asList(HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.POST);
    }

    @Override
    protected void initValidators(List<RequestValidator> requestValidators) {
        requestValidators.add(new PostURIValidator());
        requestValidators.add(new PostEmptyRequestValidator());
        requestValidators.add(new UploadDeferLengthValidator());
        requestValidators.add(new UploadLengthValidator());
    }

    @Override
    protected void initRequestHandlers(List<RequestHandler> requestHandlers) {
        requestHandlers.add(new CreationHeadRequestHandler());
        requestHandlers.add(new CreationPatchRequestHandler());
        requestHandlers.add(new CreationPostRequestHandler());
        requestHandlers.add(new CreationOptionsRequestHandler());
    }
}
