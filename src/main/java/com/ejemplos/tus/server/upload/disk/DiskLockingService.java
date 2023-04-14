package com.ejemplos.tus.server.upload.disk;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import com.ejemplos.tus.server.exception.TusException;
import com.ejemplos.tus.server.exception.UploadAlreadyLockedException;
import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadIdFactory;
import com.ejemplos.tus.server.upload.UploadLock;
import com.ejemplos.tus.server.upload.UploadLockingService;
import org.apache.commons.lang3.Validate;

/**
 * {@link UploadLockingService} implementation that uses the file system for implementing locking
 * <p/>
 * File locking can also apply to shared network drives. This way the framework supports clustering as long as
 * the upload storage directory is mounted as a shared (network) drive.
 * <p/>
 * File locks are also automatically released on application (JVM) shutdown. This means the file locking is not
 * persistent and prevents cleanup and stale lock issues.
 */
public class DiskLockingService extends AbstractDiskBasedService implements UploadLockingService {

    private static final String LOCK_SUB_DIRECTORY = "locks";

    private UploadIdFactory idFactory;

    public DiskLockingService(String storagePath) {
        super(storagePath + File.separator + LOCK_SUB_DIRECTORY);
    }

    public DiskLockingService(UploadIdFactory idFactory, String storagePath) {
        this(storagePath);
        Validate.notNull(idFactory, "The IdFactory cannot be null");
        this.idFactory = idFactory;
    }

    @Override
    public UploadLock lockUploadByUri(String requestURI) throws TusException, IOException {

        UploadId id = idFactory.readUploadId(requestURI);

        UploadLock lock = null;

        Path lockPath = getLockPath(id);
        //If lockPath is not null, we know this is a valid Upload URI
        if (lockPath != null) {
            lock = new FileBasedLock(requestURI, lockPath);
        }
        return lock;
    }

    @Override
    public void cleanupStaleLocks() throws IOException {
        try (DirectoryStream<Path> locksStream = Files.newDirectoryStream(getStoragePath())) {
            for (Path path : locksStream) {

                FileTime lastModifiedTime = Files.getLastModifiedTime(path);
                if (lastModifiedTime.toMillis() < System.currentTimeMillis() - 10000L) {
                    UploadId id = new UploadId(path.getFileName().toString());

                    if (!isLocked(id)) {
                        Files.deleteIfExists(path);
                    }
                }

            }
        }
    }

    @Override
    public boolean isLocked(UploadId id) {
        boolean locked = true;
        Path lockPath = getLockPath(id);

        if (lockPath != null) {
            //Try to obtain a lock to see if the upload is currently locked
            try (UploadLock ignored = new FileBasedLock(id.toString(), lockPath)) {

                //We got the lock, so it means no one else is locking it.
                locked = false;

            } catch (UploadAlreadyLockedException | IOException e) {
                //There was already a lock
                locked = true;
            }
        }

        return locked;
    }

    @Override
    public void setIdFactory(UploadIdFactory idFactory) {
        Validate.notNull(idFactory, "The IdFactory cannot be null");
        this.idFactory = idFactory;
    }

    private Path getLockPath(UploadId id) {
        return getPathInStorageDirectory(id);
    }

}
