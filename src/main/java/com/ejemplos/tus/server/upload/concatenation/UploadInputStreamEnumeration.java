package com.ejemplos.tus.server.upload.concatenation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.ejemplos.tus.server.exception.UploadNotFoundException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration class that enumerates all input streams associated with given list of uploads
 */
public class UploadInputStreamEnumeration implements Enumeration<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(UploadInputStreamEnumeration.class);

    private final UploadStorageService uploadStorageService;
    private Iterator<UploadInfo> uploadIterator;
    private InputStream currentInputStream = null;

    public UploadInputStreamEnumeration(List<UploadInfo> uploadList,
                                        UploadStorageService uploadStorageService) {
        List<UploadInfo> uploads = new ArrayList<>(uploadList);
        this.uploadStorageService = uploadStorageService;
        this.uploadIterator = uploads.iterator();
    }

    @Override
    public boolean hasMoreElements() {
        if (uploadIterator != null && uploadIterator.hasNext()) {
            currentInputStream = getNextInputStream();
        } else {
            currentInputStream = null;
        }

        //if we could not get a next upload stream, set the iterator to null
        // to make sure repeated calls give the same result
        if (currentInputStream == null) {
            uploadIterator = null;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public InputStream nextElement() {
        return currentInputStream;
    }

    private InputStream getNextInputStream() {
        InputStream is = null;
        UploadInfo info = uploadIterator.next();
        if (info != null) {
            try {
                is = uploadStorageService.getUploadedBytes(info.getId());
            } catch (IOException | UploadNotFoundException ex) {
                log.error("Error while retrieving input stream for upload with ID " + info.getId(), ex);
            }
        }
        return is;
    }

}
