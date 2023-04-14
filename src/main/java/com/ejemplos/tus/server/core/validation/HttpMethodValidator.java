package com.ejemplos.tus.server.core.validation;

import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.exception.UnsupportedMethodException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import jakarta.servlet.http.HttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;

/**
 * Class to validate if the current HTTP method is valid
 */
public class HttpMethodValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey) throws TusException {

        if (method == null) {
            throw new UnsupportedMethodException("The HTTP method " + request.getMethod() + " is not supported");
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return true;
    }

}
