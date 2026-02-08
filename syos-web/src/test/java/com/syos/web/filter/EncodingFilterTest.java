package com.syos.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncodingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private FilterConfig filterConfig;

    private EncodingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new EncodingFilter();
    }

    @Test
    @DisplayName("Should set encoding and headers")
    void shouldSetEncodingAndHeaders() throws IOException, ServletException {
        // Given
        when(request.getCharacterEncoding()).thenReturn(null);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not override existing request encoding")
    void shouldNotOverrideExistingRequestEncoding() throws IOException, ServletException {
        // Given
        when(request.getCharacterEncoding()).thenReturn("ISO-8859-1");

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(request, never()).setCharacterEncoding(anyString());
        verify(response).setCharacterEncoding("UTF-8");
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
