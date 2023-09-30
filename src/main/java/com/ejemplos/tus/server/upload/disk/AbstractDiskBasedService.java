package com.ejemplos.tus.server.upload.disk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ejemplos.tus.server.upload.UploadId;
import org.apache.commons.lang3.Validate;

/**
 * Common abstract super class to implement service that use the disk file system
 */
public class AbstractDiskBasedService {

    private final Path storagePath;

    /**
     * Assign disk path
     * @param path Path to disk
     */
    public AbstractDiskBasedService(String path) {
        Validate.notBlank(path, "The storage path cannot be blank");
        this.storagePath = Paths.get(path);
    }

    /**
     * @return The storage path
     */
    protected Path getStoragePath() {
        return storagePath;
    }

    /**
     * @param id Upload id
     * @return Upload id
     */
    protected Path getPathInStorageDirectory(UploadId id) {
        if (!Files.exists(storagePath)) {
            init();
        }

        if (id == null) {
            return null;
        } else {
            return storagePath.resolve(id.toString());
        }
    }

    /**
     *
     */
    private synchronized void init() {
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
            } catch (IOException e) {
                String message = "Unable to create the directory specified by the storage path " + storagePath;
                throw new StoragePathNotAvailableException(message, e);
            }
        }
    }
}
