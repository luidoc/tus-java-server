package com.ejemplos.tus.server.upload.disk;

import com.ejemplos.tus.server.exception.UploadAlreadyLockedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileBasedLockTest {

    private static Path storagePath;

    @BeforeAll
    public static void setupDataFolder() throws IOException {
        storagePath = Paths.get("target", "tus", "locks").toAbsolutePath();
        Files.createDirectories(storagePath);
    }

    @Test
    void testLockRelease() throws UploadAlreadyLockedException, IOException {
        UUID test = UUID.randomUUID();
        FileBasedLock lock = new FileBasedLock("/test/upload/" + test.toString(), storagePath.resolve(test.toString()));
        lock.release();
        lock.close();
        assertFalse(Files.exists(storagePath.resolve(test.toString())));
    }

    @Test
    void testOverlappingLock() {
        assertThrows(UploadAlreadyLockedException.class, () -> {

            UUID test = UUID.randomUUID();
            Path path = storagePath.resolve(test.toString());
            try (FileBasedLock lock1 = new FileBasedLock("/test/upload/" + test.toString(), path)) {
                FileBasedLock lock2 = new FileBasedLock("/test/upload/" + test.toString(), path);
                lock2.close();
            }
        });
    }

    @Test
    void testAlreadyLocked() {
        assertThrows(UploadAlreadyLockedException.class, () -> {

            UUID test1 = UUID.randomUUID();
            Path path1 = storagePath.resolve(test1.toString());
            try (FileBasedLock lock1 = new FileBasedLock("/test/upload/" + test1.toString(), path1)) {
                FileBasedLock lock2 = new FileBasedLock("/test/upload/" + test1.toString(), path1) {
                    @Override
                    protected FileChannel createFileChannel() throws IOException {
                        FileChannel channel = createFileChannelMock();
                        doReturn(null).when(channel).tryLock(anyLong(), anyLong(), anyBoolean());
                        return channel;
                    }
                };
                lock2.close();
            }
        });
    }

    @Test
    void testLockReleaseLockRelease() throws UploadAlreadyLockedException, IOException {
        UUID test = UUID.randomUUID();
        Path path = storagePath.resolve(test.toString());
        FileBasedLock lock = new FileBasedLock("/test/upload/" + test.toString(), path);
        lock.release();
        lock.close();
        assertFalse(Files.exists(path));
        lock = new FileBasedLock("/test/upload/" + test.toString(), path);
        lock.release();
        lock.close();
        assertFalse(Files.exists(path));
    }

    @Test
    void testLockIOException() {
        // Create directory on place where lock file will be
        assertThrows(IOException.class, () -> {

            UUID test = UUID.randomUUID();
            Path path = storagePath.resolve(test.toString());
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                fail();
            }

            FileBasedLock lock = new FileBasedLock("/test/upload/" + test.toString(), path);
            lock.close();
        });
    }

    private FileChannel createFileChannelMock() {
        return spy(FileChannel.class);
    }
}