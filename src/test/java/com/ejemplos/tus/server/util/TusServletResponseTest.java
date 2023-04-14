package com.ejemplos.tus.server.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class TusServletResponseTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    private TusServletResponse tusServletResponse;
    private MockHttpServletResponse servletResponse;

    @BeforeEach
    public void setUp() {
        servletResponse = new MockHttpServletResponse();
        tusServletResponse = new TusServletResponse(servletResponse);
        DATE_FORMAT.setTimeZone(GMT);
        DATE_FORMAT1.setTimeZone(GMT);
    }

    @Test
    public void setDateHeader() throws Exception {
        tusServletResponse.setDateHeader("TEST", DATE_FORMAT.parse("14-04-2023 22:34:14").getTime());
        tusServletResponse.setDateHeader("TEST", DATE_FORMAT.parse("14-04-2023 22:38:14").getTime());

        assertThat(tusServletResponse.getHeader("TEST"),
                is(String.valueOf(DATE_FORMAT.parse("14-04-2023 22:38:14").getTime())));
        assertThat(servletResponse.getHeaders("TEST"),
                contains(DATE_FORMAT1.format(DATE_FORMAT.parse("14-04-2023 22:38:14").getTime())));
    }

    @Test
    public void addDateHeader() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(GMT);

        tusServletResponse.addDateHeader("TEST", DATE_FORMAT.parse("14-04-2023 22:34:12").getTime());
        tusServletResponse.addDateHeader("TEST", DATE_FORMAT.parse("14-04-2023 22:38:14").getTime());

        assertThat(tusServletResponse.getHeader("TEST"),
                is(String.valueOf(DATE_FORMAT.parse("14-04-2023 22:34:12").getTime())));
        DateFormat dateFormat1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormat1.setTimeZone(GMT);
        assertThat(servletResponse.getHeaders("TEST"), containsInAnyOrder(
                DATE_FORMAT1.format(DATE_FORMAT.parse("14-04-2023 22:34:12").getTime()),
                DATE_FORMAT1.format(DATE_FORMAT.parse("14-04-2023 22:38:14").getTime())));
    }

    @Test
    public void setHeader() throws Exception {
        tusServletResponse.setHeader("TEST", "foo");
        tusServletResponse.setHeader("TEST", "bar");

        assertThat(tusServletResponse.getHeader("TEST"), is("bar"));
        assertThat(servletResponse.getHeaders("TEST"), contains("bar"));
    }

    @Test
    public void addHeader() throws Exception {
        tusServletResponse.addHeader("TEST", "foo");
        tusServletResponse.addHeader("TEST", "bar");

        assertThat(tusServletResponse.getHeader("TEST"), is("foo"));
        assertThat(servletResponse.getHeaders("TEST"), containsInAnyOrder("foo", "bar"));
    }

    @Test
    public void setIntHeader() throws Exception {
        tusServletResponse.setIntHeader("TEST", 1);
        tusServletResponse.setIntHeader("TEST", 2);

        assertThat(tusServletResponse.getHeader("TEST"), is("2"));
        assertThat(servletResponse.getHeaders("TEST"), contains("2"));
    }

    @Test
    public void addIntHeader() throws Exception {
        tusServletResponse.addIntHeader("TEST", 1);
        tusServletResponse.addIntHeader("TEST", 2);

        assertThat(tusServletResponse.getHeader("TEST"), is("1"));
        assertThat(servletResponse.getHeaders("TEST"), contains("1", "2"));
    }

    @Test
    public void getHeaderNull() throws Exception {
        assertThat(tusServletResponse.getHeader("TEST"), is(nullValue()));
    }

}