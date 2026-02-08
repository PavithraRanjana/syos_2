package com.syos.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private FilterConfig filterConfig;

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate request ID if missing")
    void shouldGenerateRequestIdIfMissing() throws IOException, ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(chain).doFilter(request, response);
        // MDC is cleared in finally block, so we check side effects
    }

    @Test
    @DisplayName("Should use existing request ID")
    void shouldUseExistingRequestId() throws IOException, ServletException {
        // Given
        String existingId = "test-123";
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("X-Request-ID")).thenReturn(existingId);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).setHeader("X-Request-ID", existingId);
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip static resources")
    void shouldSkipStaticResources() throws IOException, ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/static/style.css");

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response, never()).setHeader(eq("X-Request-ID"), anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Init and Destroy should ensure coverage")
    void initAndDestroy() throws ServletException {
        filter.init(filterConfig);
        filter.destroy();
        // No exceptions expected
    }
}
