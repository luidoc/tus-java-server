package com.ejemplos.tus.server.creation;

import java.io.IOException;

import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.upload.UploadType;
import com.ejemplos.tus.server.util.AbstractRequestHandler;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

/** A HEAD request can be used to retrieve the metadata that was supplied at creation.
 * <p>
 *  If an upload contains additional metadata, responses to HEAD requests MUST include the Upload-Metadata
 *  header and its value as specified by the Client during the creation.
 * <p>
 *  As long as the length of the upload is not known, the Server MUST set Upload-Defer-Length: 1 in
 *  all responses to HEAD requests.
 */
public class CreationHeadRequestHandler extends AbstractRequestHandler {

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.HEAD.equals(method);
    }

    @Override
    public void process(HttpMethod method, TusServletRequest servletRequest,
                        TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                        String ownerKey) throws IOException {

        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(servletRequest.getRequestURI(), ownerKey);

        if (uploadInfo.hasMetadata()) {
            servletResponse.setHeader(HttpHeader.UPLOAD_METADATA, uploadInfo.getEncodedMetadata());
        }

        if (!uploadInfo.hasLength() && !UploadType.CONCATENATED.equals(uploadInfo.getUploadType())) {
            servletResponse.setHeader(HttpHeader.UPLOAD_DEFER_LENGTH, "1");
        }
    }
}
