package com.ejemplos.tus.server.upload;

import com.ejemplos.tus.server.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeBasedUploadIdFactoryTest {

    private UploadIdFactory idFactory;

    @BeforeEach
    public void setUp() {
        idFactory = new TimeBasedUploadIdFactory();
    }

    @Test
    public void setUploadURINull() throws Exception {
        Throwable exception =
                assertThrows(NullPointerException.class, () -> {

                    idFactory.setUploadURI(null);
                });
    }

    @Test
    public void setUploadURINoTrailingSlash() throws Exception {
        idFactory.setUploadURI("/test/upload");
        assertThat(idFactory.getUploadURI(), is("/test/upload"));
    }

    @Test
    public void setUploadURIWithTrailingSlash() throws Exception {
        idFactory.setUploadURI("/test/upload/");
        assertThat(idFactory.getUploadURI(), is("/test/upload/"));
    }

    @Test
    public void setUploadURIBlank() throws Exception {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> {

                    idFactory.setUploadURI(" ");
                });
    }

    @Test
    public void setUploadURINoStartingSlash() throws Exception {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> {

                    idFactory.setUploadURI("test/upload/");
                });
    }

    @Test
    public void setUploadURIEndsWithDollar() throws Exception {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> {

                    idFactory.setUploadURI("/test/upload$");
                });
    }

    @Test
    public void readUploadId() throws Exception {
        idFactory.setUploadURI("/test/upload");

        assertThat(idFactory.readUploadId("/test/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    public void readUploadIdRegex() throws Exception {
        idFactory.setUploadURI("/users/[0-9]+/files/upload");

        assertThat(idFactory.readUploadId("/users/1337/files/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    public void readUploadIdTrailingSlash() throws Exception {
        idFactory.setUploadURI("/test/upload/");

        assertThat(idFactory.readUploadId("/test/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    public void readUploadIdRegexTrailingSlash() throws Exception {
        idFactory.setUploadURI("/users/[0-9]+/files/upload/");

        assertThat(idFactory.readUploadId("/users/123456789/files/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    public void readUploadIdNoUUID() throws Exception {
        idFactory.setUploadURI("/test/upload");

        assertThat(idFactory.readUploadId("/test/upload/not-a-time-value"), is(nullValue()));
    }

    @Test
    public void readUploadIdRegexNoMatch() throws Exception {
        idFactory.setUploadURI("/users/[0-9]+/files/upload");

        assertThat(idFactory.readUploadId("/users/files/upload/1546152320043"),
                is(nullValue()));
    }

    @Test
    public void createId() throws Exception {
        UploadId id = idFactory.createId();
        assertThat(id, not(nullValue()));
        Utils.sleep(10);
        assertThat(Long.parseLong(id.getOriginalObject().toString()),
                greaterThan(System.currentTimeMillis() - 1000L));
        assertThat(Long.parseLong(id.getOriginalObject().toString()),
                lessThan(System.currentTimeMillis()));
    }
}