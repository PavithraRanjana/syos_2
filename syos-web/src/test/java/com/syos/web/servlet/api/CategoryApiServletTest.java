package com.syos.web.servlet.api;

import com.syos.domain.models.Category;
import com.syos.domain.models.Subcategory;
import com.syos.repository.interfaces.CategoryRepository;
import com.syos.repository.interfaces.SubcategoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CategoryApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    private CategoryApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CategoryApiServlet();
        // Inject mocks via reflection
        java.lang.reflect.Field catField = CategoryApiServlet.class.getDeclaredField("categoryRepository");
        catField.setAccessible(true);
        catField.set(servlet, categoryRepository);

        java.lang.reflect.Field subField = CategoryApiServlet.class.getDeclaredField("subcategoryRepository");
        subField.setAccessible(true);
        subField.set(servlet, subcategoryRepository);

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("doGet tests")
    class DoGetTests {

        @Test
        @DisplayName("Should list all categories")
        void shouldListAllCategories() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            Category cat = new Category(1, "Electronics", "ELEC");
            when(categoryRepository.findAll()).thenReturn(List.of(cat));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(categoryRepository).findAll();
            printWriter.flush();
            assert responseWriter.toString().contains("categories");
        }

        @Test
        @DisplayName("Should list categories with root path")
        void shouldListCategoriesWithRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(categoryRepository.findAll()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(categoryRepository).findAll();
        }

        @Test
        @DisplayName("Should get category by id")
        void shouldGetCategoryById() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1");
            Category cat = new Category(1, "Electronics", "ELEC");
            when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(categoryRepository).findById(1);
        }

        @Test
        @DisplayName("Should return 404 for non-existent category")
        void shouldReturn404ForNonExistentCategory() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/999");
            when(categoryRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 400 for invalid category id")
        void shouldReturn400ForInvalidCategoryId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should get subcategories for category")
        void shouldGetSubcategoriesForCategory() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/subcategories");
            Subcategory sub = new Subcategory(1, "Phones", "PHO", 1);
            when(subcategoryRepository.findByCategoryId(1)).thenReturn(List.of(sub));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(subcategoryRepository).findByCategoryId(1);
            printWriter.flush();
            assert responseWriter.toString().contains("subcategories");
        }

        @Test
        @DisplayName("Should return 400 for invalid category id in subcategories")
        void shouldReturn400ForInvalidCategoryIdInSubcategories() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc/subcategories");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
