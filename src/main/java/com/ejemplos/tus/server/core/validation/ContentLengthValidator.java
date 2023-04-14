package com.ejemplos.tus.server.core.validation;

import java.io.IOException;

import com.ejemplos.tus.server.exception.InvalidContentLengthException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.Utils;
import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Validate that the given upload length in combination with the bytes we already received,
 * does not exceed the declared initial length on upload creation.
 */
public class ContentLengthValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException, IOException {

        Long contentLength = Utils.getLongHeader(request, HttpHeader.CONTENT_LENGTH);

        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(request.getRequestURI(), ownerKey);

        if (contentLength != null
                && uploadInfo != null
                && uploadInfo.hasLength()
                && (uploadInfo.getOffset() + contentLength > uploadInfo.getLength())) {

            throw new InvalidContentLengthException("The " + HttpHeader.CONTENT_LENGTH + " value " + contentLength
                    + " in combination with the current offset " + uploadInfo.getOffset()
                    + " exceeds the declared upload length " + uploadInfo.getLength());
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.PATCH.equals(method);
    }

}
