package com.ejemplos.tus.server.core;

import java.io.IOException;
import java.util.Objects;

import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.exception.UploadNotFoundException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.AbstractRequestHandler;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

/**
 * The Server SHOULD accept PATCH requests against any upload URL and apply the bytes contained in the message at
 * the given offset specified by the Upload-Offset header.
 * <p>
 * The Server MUST acknowledge successful PATCH requests with the 204 No Content status. It MUST include the
 * Upload-Offset header containing the new offset. The new offset MUST be the sum of the offset before the PATCH
 * request and the number of bytes received and processed or stored during the current PATCH request.
 */
public class CorePatchRequestHandler extends AbstractRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(CorePatchRequestHandler.class);

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.PATCH.equals(method);
    }

    @Override
    public void process(HttpMethod method, TusServletRequest servletRequest,
                        TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                        String ownerKey) throws IOException, TusException {

        boolean found = true;
        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(servletRequest.getRequestURI(), ownerKey);

        if (uploadInfo == null) {
            found = false;
        } else if (uploadInfo.isUploadInProgress()) {
            try {
                uploadInfo = uploadStorageService.append(uploadInfo, servletRequest.getContentInputStream());
            } catch (UploadNotFoundException e) {
                found = false;
            }
        }

        if (found) {
            servletResponse.setHeader(HttpHeader.UPLOAD_OFFSET, Objects.toString(uploadInfo.getOffset()));
            servletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);

            if (!uploadInfo.isUploadInProgress()) {
                log.info("Upload with ID {} at location {} finished successfully",
                        uploadInfo.getId(), servletRequest.getRequestURI());
            }
        } else {
            log.error("The patch request handler could not find the upload for URL {}. This means something is really wrong the request validators!",servletRequest.getRequestURI());
            servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
