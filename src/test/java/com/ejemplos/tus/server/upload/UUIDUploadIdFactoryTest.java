package com.ejemplos.tus.server.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UUIDUploadIdFactoryTest {

    private UploadIdFactory idFactory;

    @BeforeEach
    public void setUp() {
        idFactory = new UUIDUploadIdFactory();
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

        assertThat(idFactory.readUploadId("/test/upload/1911e8a4-6939-490c-b58b-a5d70f8d91fb"),
                hasToString("1911e8a4-6939-490c-b58b-a5d70f8d91fb"));
    }

    @Test
    void readUploadIdRegex() {
        idFactory.setUploadURI("/users/[0-9]+/files/upload");

        assertThat(idFactory.readUploadId("/users/1337/files/upload/1911e8a4-6939-490c-b58b-a5d70f8d91fb"),
                hasToString("1911e8a4-6939-490c-b58b-a5d70f8d91fb"));
    }

    @Test
    void readUploadIdTrailingSlash() {
        idFactory.setUploadURI("/test/upload/");

        assertThat(idFactory.readUploadId("/test/upload/1911e8a4-6939-490c-b58b-a5d70f8d91fb"),
                hasToString("1911e8a4-6939-490c-b58b-a5d70f8d91fb"));
    }

    @Test
    void readUploadIdRegexTrailingSlash() {
        idFactory.setUploadURI("/users/[0-9]+/files/upload/");

        assertThat(idFactory.readUploadId("/users/123456789/files/upload/1911e8a4-6939-490c-b58b-a5d70f8d91fb"),
                hasToString("1911e8a4-6939-490c-b58b-a5d70f8d91fb"));
    }

    @Test
    void readUploadIdNoUUID() {
        idFactory.setUploadURI("/test/upload");

        assertThat(idFactory.readUploadId("/test/upload/not-a-uuid-value"), is(nullValue()));
    }

    @Test
    void readUploadIdRegexNoMatch() {
        idFactory.setUploadURI("/users/[0-9]+/files/upload");

        assertThat(idFactory.readUploadId("/users/files/upload/1911e8a4-6939-490c-b58b-a5d70f8d91fb"),
                is(nullValue()));
    }

    @Test
    void createId() {
        assertThat(idFactory.createId(), not(nullValue()));
    }

}