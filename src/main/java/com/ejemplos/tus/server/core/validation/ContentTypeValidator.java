package com.ejemplos.tus.server.core.validation;

import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.Utils;
import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;

/**
 * All PATCH requests MUST use Content-Type: application/offset+octet-stream.
 */
public class ContentTypeValidator implements RequestValidator {

    static final String APPLICATION_OFFSET_OCTET_STREAM = "application/offset+octet-stream";

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey) throws TusException {

        String contentType = Utils.getHeader(request, HttpHeader.CONTENT_TYPE);
        if (!APPLICATION_OFFSET_OCTET_STREAM.equals(contentType)) {
            throw new InvalidContentTypeException("The " + HttpHeader.CONTENT_TYPE + " header must contain value "
                    + APPLICATION_OFFSET_OCTET_STREAM);
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.PATCH.equals(method);
    }

}
