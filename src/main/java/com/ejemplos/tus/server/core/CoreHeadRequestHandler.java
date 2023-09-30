package com.ejemplos.tus.server.core;

import java.io.IOException;
import java.util.Objects;

import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.upload.UploadType;
import com.ejemplos.tus.server.util.AbstractRequestHandler;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;

/** A HEAD request is used to determine the offset at which the upload should be continued.
 * <p>
 * The Server MUST always include the Upload-Offset header in the response for a HEAD request,
 * even if the offset is 0, or the upload is already considered completed. If the size of the upload is known,
 * the Server MUST include the Upload-Length header in the response.
 * <p>
 * The Server MUST prevent the client and/or proxies from caching the response by adding
 * the Cache-Control: no-store header to the response.
 */
public class CoreHeadRequestHandler extends AbstractRequestHandler {

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.HEAD.equals(method);
    }

    @Override
    public void process(HttpMethod method, TusServletRequest servletRequest,
                        TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                        String ownerKey) throws IOException {

        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(servletRequest.getRequestURI(), ownerKey);

        if (!UploadType.CONCATENATED.equals(uploadInfo.getUploadType())) {

            if (uploadInfo.hasLength()) {
                servletResponse.setHeader(HttpHeader.UPLOAD_LENGTH, Objects.toString(uploadInfo.getLength()));
            }
            servletResponse.setHeader(HttpHeader.UPLOAD_OFFSET, Objects.toString(uploadInfo.getOffset()));
        }

        servletResponse.setHeader(HttpHeader.CACHE_CONTROL, "no-store");

        servletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
