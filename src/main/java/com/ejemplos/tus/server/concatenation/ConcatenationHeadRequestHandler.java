package com.ejemplos.tus.server.concatenation;

import java.io.IOException;
import java.util.Objects;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.upload.UploadType;
import com.ejemplos.tus.server.util.AbstractRequestHandler;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;

/**
 * The response to a HEAD request for a upload SHOULD NOT contain the Upload-Offset header unless the
 * concatenation has been successfully finished. After successful concatenation, the Upload-Offset and
 * Upload-Length MUST be set and their values MUST be equal. The value of the Upload-Offset header before
 * concatenation is not defined for a upload.
 * <p/>
 * The response to a HEAD request for a partial upload MUST contain the Upload-Offset header. Response to HEAD
 * request against partial or upload MUST include the Upload-Concat header and its value as received in
 * the upload creation request.
 */
public class ConcatenationHeadRequestHandler extends AbstractRequestHandler {

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.HEAD.equals(method);
    }

    @Override
    public void process(HttpMethod method, TusServletRequest servletRequest,
                        TusServletResponse servletResponse, UploadStorageService uploadStorageService,
                        String ownerKey) throws IOException, TusException {

        UploadInfo uploadInfo = uploadStorageService.getUploadInfo(servletRequest.getRequestURI(), ownerKey);

        if (!UploadType.REGULAR.equals(uploadInfo.getUploadType())) {
            servletResponse.setHeader(HttpHeader.UPLOAD_CONCAT, uploadInfo.getUploadConcatHeaderValue());
        }

        if (UploadType.CONCATENATED.equals(uploadInfo.getUploadType())) {
            if (uploadInfo.isUploadInProgress()) {
                //Execute the merge function again to update our upload data
                uploadStorageService.getUploadConcatenationService().merge(uploadInfo);
            }

            if (uploadInfo.hasLength()) {
                servletResponse.setHeader(HttpHeader.UPLOAD_LENGTH, Objects.toString(uploadInfo.getLength()));
            }

            if (!uploadInfo.isUploadInProgress()) {
                servletResponse.setHeader(HttpHeader.UPLOAD_OFFSET, Objects.toString(uploadInfo.getOffset()));
            }
        }
    }
}
