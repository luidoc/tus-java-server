package com.ejemplos.tus.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ejemplos.tus.server.checksum.ChecksumAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.TusExtension;

public class TusServletRequest extends HttpServletRequestWrapper {

    private CountingInputStream countingInputStream;
    private final Map<ChecksumAlgorithm, DigestInputStream> digestInputStreamMap = new EnumMap<ChecksumAlgorithm, DigestInputStream>(ChecksumAlgorithm.class);

    private InputStream contentInputStream = null;
    private boolean isChunkedTransferDecodingEnabled = true;

    private final Map<String, List<String>> trailerHeaders = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
    private final Set<String> processedBySet = new TreeSet<String>();

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The upload request we need to wrap
     * @param isChunkedTransferDecodingEnabled Should this request wrapper decode a chunked input stream
     * @throws IllegalArgumentException if the request is null
     */
    public TusServletRequest(HttpServletRequest request, boolean isChunkedTransferDecodingEnabled) {
        super(request);
        this.isChunkedTransferDecodingEnabled = isChunkedTransferDecodingEnabled;
    }

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The upload request we need to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public TusServletRequest(HttpServletRequest request) {
        this(request, false);
    }

    public InputStream getContentInputStream() throws IOException {
        if (contentInputStream == null) {
            contentInputStream = super.getInputStream();

            //If we're dealing with chunked transfer encoding,
            //abstract it so that the rest of our code doesn't need to care
            boolean isChunked = hasChunkedTransferEncoding();
            if (isChunked && isChunkedTransferDecodingEnabled) {
                contentInputStream = new HttpChunkedEncodingInputStream(contentInputStream, trailerHeaders);
            }

            countingInputStream = new CountingInputStream(contentInputStream);
            contentInputStream = countingInputStream;

            ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.forUploadChecksumHeader(
                    getHeader(HttpHeader.UPLOAD_CHECKSUM));

            List<ChecksumAlgorithm> algorithms;

            if (isChunked) {
                //Since the Checksum header can still come at the end, keep track of all checksums
                algorithms = Arrays.asList(ChecksumAlgorithm.values());
            } else if (checksumAlgorithm != null) {
                algorithms = Collections.singletonList(checksumAlgorithm);
            } else {
                algorithms = Collections.emptyList();
            }

            for (ChecksumAlgorithm algorithm : algorithms) {
                DigestInputStream is = new DigestInputStream(contentInputStream, algorithm.getMessageDigest());
                digestInputStreamMap.put(algorithm, is);

                contentInputStream = is;
            }
        }

        return contentInputStream;
    }

    public long getBytesRead() {
        return countingInputStream == null ? 0 : countingInputStream.getByteCount();
    }

    public boolean hasCalculatedChecksum() {
        return !digestInputStreamMap.isEmpty();
    }

    public String getCalculatedChecksum(ChecksumAlgorithm algorithm) {
        MessageDigest messageDigest = getMessageDigest(algorithm);
        return messageDigest == null ? null :
                Base64.encodeBase64String(messageDigest.digest());
    }

    /**
     * Get the set of checksum algorithms that are actively calculated within this request
     * @return The set of active checksum algorithms
     */
    public Set<ChecksumAlgorithm> getEnabledChecksums() {
        return digestInputStreamMap.keySet();
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);

        if (StringUtils.isBlank(value) && trailerHeaders.containsKey(name)) {
            List<String> values = trailerHeaders.get(name);
            if (values != null && !values.isEmpty()) {
                value = values.get(0);
            }
        }

        return value;
    }

    public boolean isProcessedBy(TusExtension processor) {
        return processedBySet.contains(processor.getName());
    }

    public void addProcessor(TusExtension processor) {
        processedBySet.add(processor.getName());
    }

    private boolean hasChunkedTransferEncoding() {
        return StringUtils.equalsIgnoreCase("chunked", getHeader(HttpHeader.TRANSFER_ENCODING));
    }

    private MessageDigest getMessageDigest(ChecksumAlgorithm algorithm) {
        if (digestInputStreamMap.containsKey(algorithm)) {
            return digestInputStreamMap.get(algorithm).getMessageDigest();
        } else {
            return null;
        }
    }
}
