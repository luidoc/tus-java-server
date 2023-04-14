package com.ejemplos.tus.server.concatenation.validation;

import java.io.IOException;

import com.ejemplos.tus.server.exception.PatchOnFinalUploadNotAllowedException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.upload.UploadType;
import jakarta.servlet.http.HttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;

/**
 * The Server MUST respond with the 403 Forbidden status to PATCH requests against an upload URL
 * and MUST NOT modify the or its partial uploads.
 */
public class PatchFinalUploadValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws IOException, TusException {

        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(request.getRequestURI(), ownerKey);

        if (uploadInfo != null && UploadType.CONCATENATED.equals(uploadInfo.getUploadType())) {
            throw new PatchOnFinalUploadNotAllowedException("You cannot send a PATCH request for a "
                    + "concatenated upload URI");
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.PATCH.equals(method);
    }
}
