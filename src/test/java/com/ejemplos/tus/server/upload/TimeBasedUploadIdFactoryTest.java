package com.ejemplos.tus.server.upload;

import com.ejemplos.tus.server.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeBasedUploadIdFactoryTest {

    private UploadIdFactory idFactory;

    @BeforeEach
    public void setUp() {
        idFactory = new TimeBasedUploadIdFactory();
    }

    @Test
    void setUploadURINull() {
                assertThrows(NullPointerException.class, () -> {

                    idFactory.setUploadURI(null);
                });
    }

    @Test
    void setUploadURINoTrailingSlash() {
        idFactory.setUploadURI("/test/upload");
        assertThat(idFactory.getUploadURI(), is("/test/upload"));
    }

    @Test
    void setUploadURIWithTrailingSlash() {
        idFactory.setUploadURI("/test/upload/");
        assertThat(idFactory.getUploadURI(), is("/test/upload/"));
    }

    @Test
    void setUploadURIBlank() {
                assertThrows(IllegalArgumentException.class, () -> {

                    idFactory.setUploadURI(" ");
                });
    }

    @Test
    void setUploadURINoStartingSlash() {
                assertThrows(IllegalArgumentException.class, () -> {

                    idFactory.setUploadURI("test/upload/");
                });
    }

    @Test
    void setUploadURIEndsWithDollar() {
                assertThrows(IllegalArgumentException.class, () -> {

                    idFactory.setUploadURI("/test/upload$");
                });
    }

    @Test
    void readUploadId() {
        idFactory.setUploadURI("/test/upload");

        assertThat(idFactory.readUploadId("/test/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    void readUploadIdRegex() {
        idFactory.setUploadURI("/users/[0-9]+/files/upload");

        assertThat(idFactory.readUploadId("/users/1337/files/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    void readUploadIdTrailingSlash() {
        idFactory.setUploadURI("/test/upload/");

        assertThat(idFactory.readUploadId("/test/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    void readUploadIdRegexTrailingSlash() {
        idFactory.setUploadURI("/users/[0-9]+/files/upload/");

        assertThat(idFactory.readUploadId("/users/123456789/files/upload/1546152320043"),
                hasToString("1546152320043"));
    }

    @Test
    void readUploadIdNoUUID() {
        idFactory.setUploadURI("/test/upload");

        assertThat(idFactory.readUploadId("/test/upload/not-a-time-value"), is(nullValue()));
    }

    @Test
    void readUploadIdRegexNoMatch() {
        idFactory.setUploadURI("/users/[0-9]+/files/upload");

        assertThat(idFactory.readUploadId("/users/files/upload/1546152320043"),
                is(nullValue()));
    }

    @Test
    void createId() throws Exception {
        UploadId id = idFactory.createId();
        assertThat(id, not(nullValue()));
        Utils.sleep(10);
        assertThat(Long.parseLong(id.getOriginalObject().toString()),
                greaterThan(System.currentTimeMillis() - 1000L));
        assertThat(Long.parseLong(id.getOriginalObject().toString()),
                lessThan(System.currentTimeMillis()));
    }
}