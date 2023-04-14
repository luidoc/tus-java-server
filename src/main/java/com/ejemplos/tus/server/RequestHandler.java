package com.ejemplos.tus.server;

import java.io.IOException;

import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;

public interface RequestHandler {

    boolean supports(HttpMethod method);

    void process(HttpMethod method, TusServletRequest servletRequest,
                 TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                 String ownerKey) throws IOException, TusException;

    boolean isErrorHandler();

}
