package com.ejemplos.tus.server.upload.disk;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadLockingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory stream filter that only accepts uploads that are still in progress and expired
 */
public class ExpiredUploadFilter implements DirectoryStream.Filter<Path> {

    private static final Logger log = LoggerFactory.getLogger(ExpiredUploadFilter.class);

    private final DiskStorageService diskStorageService;
    private final UploadLockingService uploadLockingService;

    ExpiredUploadFilter(DiskStorageService diskStorageService, UploadLockingService uploadLockingService) {
        this.diskStorageService = diskStorageService;
        this.uploadLockingService = uploadLockingService;
    }

    @Override
    public boolean accept(Path upload) throws IOException {
        UploadId id = null;
        try {
            id = new UploadId(upload.getFileName().toString());
            UploadInfo info = diskStorageService.getUploadInfo(id);

            if (info != null && info.isExpired() && !uploadLockingService.isLocked(id)) {
                return true;
            }

        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Not able to determine state of upload " + id, ex);
            }
            throw new IOException(ex);
        }

        return false;
    }
}
