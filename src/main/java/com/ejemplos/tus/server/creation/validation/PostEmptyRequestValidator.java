package com.ejemplos.tus.server.creation.validation;

import com.ejemplos.tus.server.upload.UploadStorageService;
import jakarta.servlet.http.HttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;
import com.ejemplos.tus.server.exception.InvalidContentLengthException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.util.Utils;

/**
 * An empty POST request is used to create a new upload resource.
 */
public class PostEmptyRequestValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException {

        Long contentLength = Utils.getLongHeader(request, HttpHeader.CONTENT_LENGTH);
        if (contentLength != null && contentLength > 0) {
            throw new InvalidContentLengthException("A POST request should have a Content-Length header with value "
                    + "0 and no content");
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }
}
