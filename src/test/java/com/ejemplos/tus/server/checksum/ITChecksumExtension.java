package com.ejemplos.tus.server.checksum;

import com.ejemplos.tus.server.AbstractTusExtensionIntegrationTest;
import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.exception.ChecksumAlgorithmNotSupportedException;
import com.ejemplos.tus.server.exception.UploadChecksumMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class ITChecksumExtension extends AbstractTusExtensionIntegrationTest {

    @BeforeEach
    public void setUp() throws Exception {
        servletRequest = spy(new MockHttpServletRequest());
        servletResponse = new MockHttpServletResponse();
        tusFeature = new ChecksumExtension();
        uploadInfo = null;
    }

    @Test
    public void testOptions() throws Exception {
        setRequestHeaders();

        executeCall(HttpMethod.OPTIONS, false);

        assertResponseHeader(HttpHeader.TUS_EXTENSION, "checksum", "checksum-trailer");
        assertResponseHeader(HttpHeader.TUS_CHECKSUM_ALGORITHM, "md5", "sha1", "sha256", "sha384", "sha512");
    }

    @Test
    public void testInvalidAlgorithm() {
        Throwable exception =
                assertThrows(ChecksumAlgorithmNotSupportedException.class, () -> {
                    servletRequest.addHeader(HttpHeader.UPLOAD_CHECKSUM, "test 1234567890");
                    servletRequest.setContent("Test content".getBytes());

                    executeCall(HttpMethod.PATCH, false);
                });
    }

    @Test
    public void testValidChecksumTrailerHeader() throws Exception {
        String content = "8\r\n" +
                "Mozilla \r\n" +
                "A\r\n" +
                "Developer \r\n" +
                "7\r\n" +
                "Network\r\n" +
                "0\r\n" +
                "Upload-Checksum: sha1 zYR9iS5Rya+WoH1fEyfKqqdPWWE=\r\n" +
                "\r\n";

        servletRequest.addHeader(HttpHeader.TRANSFER_ENCODING, "chunked");
        servletRequest.setContent(content.getBytes());

        try {
            executeCall(HttpMethod.PATCH, true);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testValidChecksumNormalHeader() throws Exception {
        String content = "Mozilla Developer Network";

        servletRequest.addHeader(HttpHeader.UPLOAD_CHECKSUM, "sha1 zYR9iS5Rya+WoH1fEyfKqqdPWWE=");
        servletRequest.setContent(content.getBytes());

        executeCall(HttpMethod.PATCH, true);

        verify(servletRequest, atLeastOnce()).getHeader(HttpHeader.UPLOAD_CHECKSUM);
    }

    @Test
    public void testInvalidChecksumTrailerHeader() throws Exception {
        String content = "8\r\n" +
                "Mozilla \r\n" +
                "A\r\n" +
                "Developer \r\n" +
                "7\r\n" +
                "Network\r\n" +
                "0\r\n" +
                "Upload-Checksum: sha1 zYR9iS5Rya+WoH1fEyfKqqdPWW=\r\n" +
                "\r\n";
        Throwable exception =
                assertThrows(UploadChecksumMismatchException.class, () -> {
                    servletRequest.addHeader(HttpHeader.TRANSFER_ENCODING, "chunked");
                    servletRequest.setContent(content.getBytes());

                    executeCall(HttpMethod.PATCH, true);
                });
    }

    @Test
    public void testInvalidChecksumNormalHeader() throws Exception {
        String content = "Mozilla Developer Network";
        Throwable exception =
                assertThrows(UploadChecksumMismatchException.class, () -> {
                    servletRequest.addHeader(HttpHeader.UPLOAD_CHECKSUM, "sha1 zYR9iS5Rya+WoH1fEyfKqqdPWW=");
                    servletRequest.setContent(content.getBytes());

                    executeCall(HttpMethod.PATCH, true);
                });
    }

    @Test
    public void testNoChecksum() throws Exception {
        String content = "Mozilla Developer Network";

        servletRequest.setContent(content.getBytes());

        try {
            executeCall(HttpMethod.PATCH, true);
        } catch (Exception ex) {
            fail();
        }
    }
}