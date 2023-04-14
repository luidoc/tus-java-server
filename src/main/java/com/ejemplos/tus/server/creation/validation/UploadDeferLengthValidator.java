package com.ejemplos.tus.server.creation.validation;

import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.Utils;
import jakarta.servlet.http.HttpServletRequest;

import com.ejemplos.tus.server.exception.InvalidUploadLengthException;
import com.ejemplos.tus.server.exception.TusException;
import org.apache.commons.lang3.StringUtils;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;

/**
 * The request MUST include one of the following headers:
 * a) Upload-Length to indicate the size of an entire upload in bytes.
 * b) Upload-Defer-Length: 1 if upload size is not known at the time.
 */
public class UploadDeferLengthValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException {

        boolean uploadLength = false;
        boolean deferredLength = false;
        boolean concatenatedUpload = false;

        if (StringUtils.isNumeric(Utils.getHeader(request, HttpHeader.UPLOAD_LENGTH))) {
            uploadLength = true;
        }

        if (Utils.getHeader(request, HttpHeader.UPLOAD_DEFER_LENGTH).equals("1")) {
            deferredLength = true;
        }

        String uploadConcatValue = request.getHeader(HttpHeader.UPLOAD_CONCAT);
        if (StringUtils.startsWithIgnoreCase(uploadConcatValue, "final")) {
            concatenatedUpload = true;
        }

        if (!concatenatedUpload && !uploadLength && !deferredLength) {
            throw new InvalidUploadLengthException("No valid value was found in headers " + HttpHeader.UPLOAD_LENGTH
                    + " and " + HttpHeader.UPLOAD_DEFER_LENGTH);
        } else if (uploadLength && deferredLength) {
            throw new InvalidUploadLengthException("A POST request cannot contain both " + HttpHeader.UPLOAD_LENGTH
                    + " and " + HttpHeader.UPLOAD_DEFER_LENGTH + " headers.");
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }
}
