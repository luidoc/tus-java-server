package com.ejemplos.tus.server.concatenation.validation;

import java.io.IOException;

import com.ejemplos.tus.server.exception.InvalidPartialUploadIdException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.Utils;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;

/**
 * Validate that the IDs specified in the Upload-Concat header map to an existing upload
 */
public class PartialUploadsExistValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws IOException, TusException {

        String uploadConcatValue = request.getHeader(HttpHeader.UPLOAD_CONCAT);

        if (StringUtils.startsWithIgnoreCase(uploadConcatValue, "final")) {

            for (String uploadUri : Utils.parseConcatenationIDsFromHeader(uploadConcatValue)) {

                UploadInfo uploadInfo = uploadStorageService.getUploadInfo(uploadUri, ownerKey);
                if (uploadInfo == null) {
                    throw new InvalidPartialUploadIdException("The URI " + uploadUri
                            + " in Upload-Concat header does not match an existing upload");
                }
            }
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }

}
