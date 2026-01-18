package com.syos.web.servlet.view;

import jakarta.servlet.RequestDispatcher;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private HttpSession session;

    private AuthViewServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new AuthViewServlet();
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getContextPath()).thenReturn("");
    }

    @Nested
    @DisplayName("doGet tests - Login Page")
    class DoGetLoginPageTests {

        @Test
        @DisplayName("Should show login page when not authenticated")
        void shouldShowLoginPageWhenNotAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/login");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/auth/login.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should redirect customer to shop when already authenticated")
        void shouldRedirectCustomerToShopWhenAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/login");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
            when(session.getAttribute("userRole")).thenReturn("CUSTOMER");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendRedirect("/shop");
        }

        @Test
        @DisplayName("Should redirect cashier to POS when already authenticated")
        void shouldRedirectCashierToPOSWhenAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/login");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
            when(session.getAttribute("userRole")).thenReturn("CASHIER");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendRedirect("/pos");
        }

        @Test
        @DisplayName("Should redirect admin to admin dashboard when already authenticated")
        void shouldRedirectAdminToAdminDashboardWhenAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/login");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
            when(session.getAttribute("userRole")).thenReturn("ADMIN");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendRedirect("/admin");
        }

        @Test
        @DisplayName("Should redirect manager to reports when already authenticated")
        void shouldRedirectManagerToReportsWhenAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/login");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
            when(session.getAttribute("userRole")).thenReturn("MANAGER");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendRedirect("/reports");
        }

        @Test
        @DisplayName("Should redirect inventory manager to inventory reports when already authenticated")
        void shouldRedirectInventoryManagerWhenAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/login");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
            when(session.getAttribute("userRole")).thenReturn("INVENTORY_MANAGER");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendRedirect("/inventory/reports");
        }
    }

    @Nested
    @DisplayName("doGet tests - Register Page")
    class DoGetRegisterPageTests {

        @Test
        @DisplayName("Should show register page when not authenticated")
        void shouldShowRegisterPageWhenNotAuthenticated() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/register");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/auth/register.jsp");
            verify(requestDispatcher).forward(request, response);
        }
    }

    @Nested
    @DisplayName("doGet tests - Logout")
    class DoGetLogoutTests {

        @Test
        @DisplayName("Should logout and redirect to shop")
        void shouldLogoutAndRedirectToShop() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/logout");
            when(request.getSession(false)).thenReturn(session);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(session).invalidate();
            verify(response).sendRedirect("/shop");
        }

        @Test
        @DisplayName("Should redirect to shop even without session")
        void shouldRedirectToShopEvenWithoutSession() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/logout");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendRedirect("/shop");
        }
    }

    @Nested
    @DisplayName("doPost tests")
    class DoPostTests {

        @Test
        @DisplayName("Should handle POST logout")
        void shouldHandlePostLogout() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/logout");
            when(request.getSession(false)).thenReturn(session);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(session).invalidate();
            verify(response).sendRedirect("/shop");
        }
    }
}
