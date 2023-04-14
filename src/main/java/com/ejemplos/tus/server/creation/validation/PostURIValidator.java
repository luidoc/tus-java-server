package com.ejemplos.tus.server.creation.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ejemplos.tus.server.upload.UploadStorageService;
import jakarta.servlet.http.HttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.RequestValidator;
import com.ejemplos.tus.server.exception.PostOnInvalidRequestURIException;
import com.ejemplos.tus.server.exception.TusException;

/**
 * The Client MUST send a POST request against a known upload creation URL to request a new upload resource.
 */
public class PostURIValidator implements RequestValidator {

    private Pattern uploadUriPattern = null;

    @Override
    public void validate(HttpMethod method, HttpServletRequest request,
                         UploadStorageService uploadStorageService, String ownerKey)
            throws TusException {

        Matcher uploadUriMatcher = getUploadUriPattern(uploadStorageService).matcher(request.getRequestURI());

        if (!uploadUriMatcher.matches()) {
            throw new PostOnInvalidRequestURIException("POST requests have to be sent to '"
                    + uploadStorageService.getUploadURI() + "'. ");
        }
    }

    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }

    private Pattern getUploadUriPattern(UploadStorageService uploadStorageService) {
        if (uploadUriPattern == null) {
            //A POST request should match the full URI
            uploadUriPattern = Pattern.compile("^" + uploadStorageService.getUploadURI() + "$");
        }
        return uploadUriPattern;
    }

}
