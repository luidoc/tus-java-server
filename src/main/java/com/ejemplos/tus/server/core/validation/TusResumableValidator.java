package com.ejemplos.tus.server.core.validation;

import com.ejemplos.tus.server.exception.InvalidTusResumableException;
import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.Utils;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;
import com.ejemplos.tus.server.TusFileUploadService;

/** Class that will validate if the tus version in the request corresponds to our implementation version
 * <p>
 * The Tus-Resumable header MUST be included in every request and response except for OPTIONS requests.
 * The value MUST be the version of the protocol used by the Client or the Server.
 * If the version specified by the Client is not supported by the Server, it MUST respond with the
 * 412 Precondition Failed status and MUST include the Tus-Version header into the response.
 * In addition, the Server MUST NOT process the request.
 * <p>
 * (<a href="https://tus.io/protocols/resumable-upload.html#tus-resumable">...</a>)
 */
public class TusResumableValidator implements RequestValidator {

    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException {

        String requestVersion = Utils.getHeader(request, HttpHeader.TUS_RESUMABLE);
        if (!StringUtils.equals(requestVersion, TusFileUploadService.TUS_API_VERSION)) {
            throw new InvalidTusResumableException("This server does not support tus protocol version "
                    + requestVersion);
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return !HttpMethod.OPTIONS.equals(method) && !HttpMethod.GET.equals(method);
    }
}
