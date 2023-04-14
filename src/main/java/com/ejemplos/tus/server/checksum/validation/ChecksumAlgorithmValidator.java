package com.ejemplos.tus.server.checksum.validation;

import java.io.IOException;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.checksum.ChecksumAlgorithm;
import com.ejemplos.tus.server.exception.ChecksumAlgorithmNotSupportedException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ejemplos.tus.server.RequestValidator;

/**
 * The Server MAY respond with one of the following status code: 400 Bad Request
 * if the checksum algorithm is not supported by the server
 */
public class ChecksumAlgorithmValidator implements RequestValidator {

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException, IOException {

        String uploadChecksum = request.getHeader(HttpHeader.UPLOAD_CHECKSUM);

        //If the client provided a checksum header, check that we support the algorithm
        if (StringUtils.isNotBlank(uploadChecksum)
                && ChecksumAlgorithm.forUploadChecksumHeader(uploadChecksum) == null) {

            throw new ChecksumAlgorithmNotSupportedException("The " + HttpHeader.UPLOAD_CHECKSUM + " header value "
                    + uploadChecksum + " is not supported");

        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.PATCH.equals(method);
    }
}
