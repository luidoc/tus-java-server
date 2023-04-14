package com.ejemplos.tus.server.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestHandler;
import com.ejemplos.tus.server.RequestValidator;
import com.ejemplos.tus.server.TusExtension;
import com.ejemplos.tus.server.exception.TusException;
import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractTusExtension implements TusExtension {

    private List<RequestValidator> requestValidators = new LinkedList<RequestValidator>();
    private List<RequestHandler> requestHandlers = new LinkedList<RequestHandler>();

    public AbstractTusExtension() {
        initValidators(requestValidators);
        initRequestHandlers(requestHandlers);
    }

    protected abstract void initValidators(List<RequestValidator> requestValidators);

    protected abstract void initRequestHandlers(List<RequestHandler> requestHandlers);

    @Override
    public void validate(HttpMethod method, HttpServletRequest servletRequest,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException, IOException {

        for (RequestValidator requestValidator : requestValidators) {
            if (requestValidator.supports(method)) {
                requestValidator.validate(method, servletRequest, uploadStorageService, ownerKey);
            }
        }
    }

    @Override
    public void process(HttpMethod method, TusServletRequest servletRequest,
                        TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                        String ownerKey) throws IOException, TusException {

        for (RequestHandler requestHandler : requestHandlers) {
            if (requestHandler.supports(method)) {
                requestHandler.process(method, servletRequest, servletResponse, uploadStorageService, ownerKey);
            }
        }
    }

    @Override
    public void handleError(HttpMethod method, TusServletRequest request, TusServletResponse response,
                            UploadStorageService uploadStorageService, String ownerKey)
            throws IOException, TusException {

        for (RequestHandler requestHandler : requestHandlers) {
            if (requestHandler.supports(method) && requestHandler.isErrorHandler()) {
                requestHandler.process(method, request, response, uploadStorageService, ownerKey);
            }
        }
    }
}
