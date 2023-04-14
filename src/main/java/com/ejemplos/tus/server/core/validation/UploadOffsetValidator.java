package com.ejemplos.tus.server.core.validation;

import java.io.IOException;
import java.util.Objects;

import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.exception.UploadOffsetMismatchException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.Utils;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;

/**
 * The Upload-Offset header’s value MUST be equal to the current offset of the resource.
 * If the offsets do not match, the Server MUST respond with the
 * 409 Conflict status without modifying the upload resource.
 */
public class UploadOffsetValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws IOException, TusException {

        String uploadOffset = Utils.getHeader(request, HttpHeader.UPLOAD_OFFSET);

        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(request.getRequestURI(), ownerKey);

        if (uploadInfo != null) {
            String expectedOffset = Objects.toString(uploadInfo.getOffset());
            if (!StringUtils.equals(expectedOffset, uploadOffset)) {
                throw new UploadOffsetMismatchException("The Upload-Offset was "
                        + StringUtils.trimToNull(uploadOffset) + " but expected " + expectedOffset);
            }
        }

    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.PATCH.equals(method);
    }

}
