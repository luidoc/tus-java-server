package com.ejemplos.tus.server.upload.disk;

import com.ejemplos.tus.server.exception.InvalidUploadOffsetException;
import com.ejemplos.tus.server.exception.UploadNotFoundException;
import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadIdFactory;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadLockingService;
import com.ejemplos.tus.server.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.event.annotation.AfterTestClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiskStorageServiceTest {

    public static final String UPLOAD_URL = "/upload/test";
    private DiskStorageService storageService;

    @Mock
    private UploadIdFactory idFactory;

    @Mock
    private UploadLockingService uploadLockingService;

    private static Path storagePath;

    @BeforeAll
    public static void setupDataFolder() throws IOException {
        storagePath = Paths.get("target", "tus", "data").toAbsolutePath();
        Files.createDirectories(storagePath);
    }

    @AfterTestClass
    public static void destroyDataFolder() throws IOException {
        FileUtils.deleteDirectory(storagePath.toFile());
    }

    @BeforeEach
    public void setUp() {
        reset(idFactory);
        when(idFactory.getUploadURI()).thenReturn(UPLOAD_URL);
        when(idFactory.createId()).thenReturn(new UploadId(UUID.randomUUID()));
        when(idFactory.readUploadId(nullable(String.class))).then(new Answer<UploadId>() {
            @Override
            public UploadId answer(InvocationOnMock invocation) throws Throwable {
                return new UploadId(StringUtils.substringAfter(invocation.getArguments()[0].toString(),
                        UPLOAD_URL + "/"));
            }
        });

        storageService = new DiskStorageService(idFactory, storagePath.toString());
    }

    @Test
    void getMaxUploadSize() {
        storageService.setMaxUploadSize(null);
        assertThat(storageService.getMaxUploadSize(), is(0L));

        storageService.setMaxUploadSize(0L);
        assertThat(storageService.getMaxUploadSize(), is(0L));

        storageService.setMaxUploadSize(-10L);
        assertThat(storageService.getMaxUploadSize(), is(0L));

        storageService.setMaxUploadSize(372036854775807L);
        assertThat(storageService.getMaxUploadSize(), is(372036854775807L));
    }

    @Test
    void getUploadURI() {
        assertThat(storageService.getUploadURI(), is(UPLOAD_URL));
    }

    @Test
    void create() throws Exception {
        UploadInfo info = new UploadInfo();
        info.setLength(10L);
        info.setEncodedMetadata("Encoded Metadata");

        info = storageService.create(info, null);

        assertThat(info.getId(), is(notNullValue()));
        assertThat(info.getOffset(), is(0L));
        assertThat(info.getLength(), is(10L));
        assertThat(info.getEncodedMetadata(), is("Encoded Metadata"));

        assertTrue(Files.exists(getUploadInfoPath(info.getId())));
    }

    @Test
    void getUploadInfoById() throws Exception {
        UploadInfo info = new UploadInfo();
        info.setLength(10L);
        info.setEncodedMetadata("Encoded Metadata");

        info = storageService.create(info, "John");

        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        UploadInfo readInfo = storageService.getUploadInfo(info.getId());

        assertNotSame(readInfo, info);
        assertThat(readInfo.getId(), is(info.getId()));
        assertThat(readInfo.getOffset(), is(0L));
        assertThat(readInfo.getLength(), is(10L));
        assertThat(readInfo.getEncodedMetadata(), is("Encoded Metadata"));
        assertThat(readInfo.getCreationTimestamp(), is(info.getCreationTimestamp()));
        assertThat(readInfo.getUploadType(), is(info.getUploadType()));
        assertThat(readInfo.getOwnerKey(), is(info.getOwnerKey()));
    }

    @Test
    void getUploadInfoByFakeId() throws Exception {
        UploadInfo readInfo = storageService.getUploadInfo(new UploadId(UUID.randomUUID()));
        assertThat(readInfo, is(nullValue()));
    }

    @Test
    void getUploadInfoByUrl() throws Exception {
        UploadInfo info = new UploadInfo();
        info.setLength(10L);
        info.setEncodedMetadata("Encoded Metadata");

        info = storageService.create(info, null);

        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        UploadInfo readInfo = storageService.getUploadInfo(UPLOAD_URL + "/" + info.getId(), null);

        assertNotSame(readInfo, info);
        assertThat(readInfo.getId(), is(info.getId()));
        assertThat(readInfo.getOffset(), is(0L));
        assertThat(readInfo.getLength(), is(10L));
        assertThat(readInfo.getEncodedMetadata(), is("Encoded Metadata"));
    }

    @Test
    void getUploadInfoOtherOwner() throws Exception {
        UploadInfo info = new UploadInfo();
        info.setLength(10L);
        info.setEncodedMetadata("Encoded Metadata");

        info = storageService.create(info, "foo");

        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        UploadInfo readInfo = storageService.getUploadInfo(UPLOAD_URL + "/" + info.getId(), "foo");

        assertNotSame(readInfo, info);
        assertThat(readInfo.getId(), is(info.getId()));
        assertThat(readInfo.getOffset(), is(0L));
        assertThat(readInfo.getLength(), is(10L));
        assertThat(readInfo.getEncodedMetadata(), is("Encoded Metadata"));

        assertThat(storageService.getUploadInfo(UPLOAD_URL + "/" + info.getId(), "bar"), is(nullValue()));
    }

    @Test
    void update() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setLength(10L);
        info1.setEncodedMetadata("Encoded Metadata");

        info1 = storageService.create(info1, null);

        assertTrue(Files.exists(getUploadInfoPath(info1.getId())));

        UploadInfo info2 = new UploadInfo();
        info2.setId(info1.getId());
        info2.setLength(10L);
        info2.setOffset(8L);
        info2.setEncodedMetadata("Updated Encoded Metadata");

        storageService.update(info2);

        UploadInfo readInfo = storageService.getUploadInfo(info1.getId());

        assertNotSame(readInfo, info1);
        assertNotSame(readInfo, info2);
        assertThat(info2.getId(), is(info1.getId()));
        assertThat(readInfo.getId(), is(info1.getId()));
        assertThat(readInfo.getOffset(), is(8L));
        assertThat(readInfo.getLength(), is(10L));
        assertThat(readInfo.getEncodedMetadata(), is("Updated Encoded Metadata"));
    }

    @Test
    void append() throws Exception {
        String part1 = "This is part 1";
        String part2 = "This is the second part of my upload";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) (part1.getBytes().length + part2.getBytes().length));
        info.setEncodedMetadata("Encoded Metadata");

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the first part of the upload
        storageService.append(info, IOUtils.toInputStream(part1, StandardCharsets.UTF_8));
        assertThat(new String(Files.readAllBytes(getUploadDataPath(info.getId()))), is(part1));

        UploadInfo readInfo = storageService.getUploadInfo(info.getId());

        assertThat(readInfo.getId(), is(info.getId()));
        assertThat(readInfo.getOffset(), is((long) part1.getBytes().length));
        assertThat(readInfo.getLength(), is(info.getLength()));
        assertThat(readInfo.getEncodedMetadata(), is("Encoded Metadata"));

        //Write the second part of the upload
        storageService.append(info, IOUtils.toInputStream(part2, StandardCharsets.UTF_8));
        assertThat(new String(Files.readAllBytes(getUploadDataPath(info.getId()))), is(part1 + part2));

        readInfo = storageService.getUploadInfo(info.getId());

        assertThat(readInfo.getId(), is(info.getId()));
        assertThat(readInfo.getOffset(), is(info.getLength()));
        assertThat(readInfo.getLength(), is(info.getLength()));
        assertThat(readInfo.getEncodedMetadata(), is("Encoded Metadata"));
    }

    @Test
    void appendExceedingMaxSingleUpload() throws Exception {
        String content = "This is an upload that is too large";

        storageService.setMaxUploadSize(17L);

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength(17L);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload
        storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));

        //The storage service should protect itself an only write until the maximum number of bytes allowed
        assertThat(new String(Files.readAllBytes(getUploadDataPath(info.getId()))), is("This is an upload"));
    }

    @Test
    void appendExceedingMaxMultiUpload() throws Exception {
        String part1 = "This is an ";
        String part2 = "upload that is too large";

        storageService.setMaxUploadSize(17L);

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength(17L);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload in two parts
        storageService.append(info, IOUtils.toInputStream(part1, StandardCharsets.UTF_8));
        info = storageService.getUploadInfo(info.getId());
        storageService.append(info, IOUtils.toInputStream(part2, StandardCharsets.UTF_8));

        //The storage service should protect itself an only write until the maximum number of bytes allowed
        assertThat(new String(Files.readAllBytes(getUploadDataPath(info.getId()))), is("This is an upload"));
    }

    @Test
    void appendOnFakeUpload() {
        String content = "This upload was not created before";
                assertThrows(UploadNotFoundException.class, () -> {

                    //Create our fake upload
                    UploadInfo info = new UploadInfo();
                    info.setId(new UploadId(UUID.randomUUID()));
                    info.setLength((long) (content.getBytes().length));

                    //Write the content of the upload
                    storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
                });
    }

    @Test
    void appendOnInvalidOffset() {
        String content = "This is an upload that is too large";
                assertThrows(InvalidUploadOffsetException.class, () -> {

                    storageService.setMaxUploadSize(17L);

                    //Create our upload with the correct length
                    UploadInfo info = new UploadInfo();
                    info.setLength(17L);

                    info = storageService.create(info, null);
                    assertTrue(Files.exists(getUploadInfoPath(info.getId())));

                    info.setOffset(3L);
                    storageService.update(info);

                    //Write the content of the upload
                    storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
                });
    }

    @Test
    void appendInterrupted() throws Exception {
        String content = "This is an upload that will be interrupted";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) content.getBytes().length);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        InputStream exceptionStream = mock(InputStream.class);
        doThrow(new RuntimeException()).when(exceptionStream)
                .read(org.mockito.Mockito.any(byte[].class), anyInt(), anyInt());

        InputStream sequenceStream = new SequenceInputStream(IOUtils.toInputStream(content, StandardCharsets.UTF_8),
                exceptionStream);

        try {
            //Write the content of the upload
            storageService.append(info, sequenceStream);
            fail();
        } catch (Exception ex) {
            //ignore
        }

        info = storageService.getUploadInfo(info.getId());
        assertThat(new String(Files.readAllBytes(getUploadDataPath(info.getId()))),
                is(content));
        assertThat(info.getOffset(), is((long) content.getBytes().length));
    }

    @Test
    void testRemoveLastNumberOfBytes() throws Exception {
        String content = "This is an upload that will be truncated";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength(50L);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload
        storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));

        //Now truncate
        storageService.removeLastNumberOfBytes(info, 23);

        assertThat(new String(Files.readAllBytes(getUploadDataPath(info.getId()))), is("This is an upload"));
    }

    @Test
    void getUploadedBytes() throws Exception {
        String content = "This is the content of my upload";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) content.getBytes().length);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload
        storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
        assertTrue(Files.exists(getUploadDataPath(info.getId())));

        try (InputStream uploadedBytes = storageService.getUploadedBytes(UPLOAD_URL + "/" + info.getId(), null)) {

            assertThat(IOUtils.toString(uploadedBytes, StandardCharsets.UTF_8),
                    is("This is the content of my upload"));
        }
    }

    @Test
    void copyUploadedBytes() throws Exception {
        String content = "This is the content of my upload";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) content.getBytes().length);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload
        storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
        assertTrue(Files.exists(getUploadDataPath(info.getId())));

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            storageService.copyUploadTo(info, output);
            assertThat(new String(output.toByteArray(), StandardCharsets.UTF_8),
                    is("This is the content of my upload"));
        }
    }

    @Test
    void terminateCompletedUpload() throws Exception {
        String content = "This is the content of my upload";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) content.getBytes().length);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload
        storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
        assertTrue(Files.exists(getUploadDataPath(info.getId())));

        //Now delete the upload and check the files are gone
        storageService.terminateUpload(info);
        assertFalse(Files.exists(getUploadInfoPath(info.getId())));
        assertFalse(Files.exists(getUploadDataPath(info.getId())));
        assertFalse(Files.exists(getStoragePath(info.getId())));
    }

    @Test
    void terminateInProgressUpload() throws Exception {
        String content = "This is the content of my upload";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) content.getBytes().length + 20);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        //Write the content of the upload
        storageService.append(info, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
        assertTrue(Files.exists(getUploadDataPath(info.getId())));

        //Now delete the upload and check the files are gone
        storageService.terminateUpload(info);
        assertFalse(Files.exists(getUploadInfoPath(info.getId())));
        assertFalse(Files.exists(getUploadDataPath(info.getId())));
        assertFalse(Files.exists(getStoragePath(info.getId())));

        //Call with null should not result in an error
        storageService.terminateUpload(null);
    }

    @Test
    void cleanupExpiredUploads() throws Exception {
        when(uploadLockingService.isLocked(any(UploadId.class))).thenReturn(false);

        String content = "This is the content of my upload";

        //Create our upload with the correct length
        UploadInfo info = new UploadInfo();
        info.setLength((long) content.getBytes().length + 20);
        info.updateExpiration(100L);

        info = storageService.create(info, null);
        assertTrue(Files.exists(getUploadInfoPath(info.getId())));

        Utils.sleep(500L);
        storageService.cleanupExpiredUploads(uploadLockingService);

        assertFalse(Files.exists(getUploadInfoPath(info.getId())));
        assertFalse(Files.exists(getStoragePath(info.getId())));
    }

    private Path getUploadInfoPath(UploadId id) {
        return getStoragePath(id).resolve("info");
    }

    private Path getUploadDataPath(UploadId id) {
        return getStoragePath(id).resolve("data");
    }

    private Path getStoragePath(UploadId id) {
        return storagePath.resolve("uploads").resolve(id.toString());
    }
}