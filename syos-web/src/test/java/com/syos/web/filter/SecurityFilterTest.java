package com.syos.web.filter;

import com.syos.domain.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private HttpSession session;
    @Mock
    private FilterConfig filterConfig;

    private SecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter();
        lenient().when(request.getContextPath()).thenReturn("");
    }

    @Test
    @DisplayName("Should allow static resources without auth")
    void shouldAllowStaticResources() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/static/style.css");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow public paths without auth")
    void shouldAllowPublicPaths() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/login");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {
        @Test
        @DisplayName("Should redirect unauthenticated view request to login")
        void shouldRedirectUnauthenticatedViewRequest() throws IOException, ServletException {
            when(request.getRequestURI()).thenReturn("/dashboard");
            when(request.getSession(false)).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(response).sendRedirect(contains("/login"));
            verify(chain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated API request")
        void shouldReturn401ForUnauthenticatedApiRequest() throws IOException, ServletException {
            when(request.getRequestURI()).thenReturn("/api/admin");
            when(request.getSession(false)).thenReturn(null);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            filter.doFilter(request, response, chain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(chain, never()).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @BeforeEach
        void setupAuth() {
            when(request.getSession(false)).thenReturn(session);
            lenient().when(request.getSession()).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
        }

        @Test
        @DisplayName("Admin should have access to everything")
        void adminShouldHaveFullAccess() throws IOException, ServletException {
            when(request.getRequestURI()).thenReturn("/admin/users");
            when(session.getAttribute("userRole")).thenReturn(UserRole.ADMIN.name());

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("Customer should access shop paths but not admin")
        void customerAccess() throws IOException, ServletException {
            // Valid access
            when(request.getRequestURI()).thenReturn("/cart");
            when(session.getAttribute("userRole")).thenReturn(UserRole.CUSTOMER.name());
            filter.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);

            // Invalid access (reset chain mock)
            clearInvocations(chain);
            when(request.getRequestURI()).thenReturn("/admin");

            filter.doFilter(request, response, chain);
            verify(chain, never()).doFilter(request, response);
            verify(response).sendRedirect(contains("error=access_denied"));
        }

        @Test
        @DisplayName("Cashier should access POS but not customer paths")
        void cashierAccess() throws IOException, ServletException {
            when(request.getRequestURI()).thenReturn("/pos");
            when(session.getAttribute("userRole")).thenReturn(UserRole.CASHIER.name());
            filter.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);

            clearInvocations(chain);
            when(request.getRequestURI()).thenReturn("/cart"); // Shopping not allowed
            filter.doFilter(request, response, chain);
            verify(chain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 403 for unauthorized API request")
        void shouldReturn403ForUnauthorizedApiRequest() throws IOException, ServletException {
            when(request.getRequestURI()).thenReturn("/api/admin/users");
            when(session.getAttribute("userRole")).thenReturn(UserRole.CUSTOMER.name());

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            filter.doFilter(request, response, chain);

            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(chain, never()).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Init and Destroy should ensure coverage")
    void initAndDestroy() throws ServletException {
        filter.init(filterConfig);
        filter.destroy();
        // No exceptions expected
    }
}
