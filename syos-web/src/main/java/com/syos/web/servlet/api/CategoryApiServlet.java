package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Category;
import com.syos.domain.models.Subcategory;
import com.syos.repository.interfaces.CategoryRepository;
import com.syos.repository.interfaces.SubcategoryRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API servlet for Category and Subcategory operations.
 *
 * Endpoints:
 * GET /api/categories - List all categories
 * GET /api/categories/{id} - Get category by ID
 * GET /api/categories/{id}/subcategories - Get subcategories for a category
 */
@WebServlet(urlPatterns = { "/api/categories", "/api/categories/*" })
public class CategoryApiServlet extends BaseApiServlet {

    private CategoryRepository categoryRepository;
    private SubcategoryRepository subcategoryRepository;

    @Override
    public void init() throws ServletException {
        super.init();
        categoryRepository = ServiceRegistry.get(CategoryRepository.class);
        subcategoryRepository = ServiceRegistry.get(SubcategoryRepository.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/categories - List all
                handleListCategories(response);
            } else {
                String[] parts = pathInfo.substring(1).split("/");

                if (parts.length == 1) {
                    // GET /api/categories/{id}
                    handleGetCategory(parts[0], response);
                } else if (parts.length == 2 && "subcategories".equals(parts[1])) {
                    // GET /api/categories/{id}/subcategories
                    handleGetSubcategories(parts[0], response);
                } else {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
                }
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleListCategories(HttpServletResponse response) throws IOException {
        List<Category> categories = categoryRepository.findAll();

        List<Map<String, Object>> categoryData = categories.stream()
                .map(cat -> Map.<String, Object>of(
                        "categoryId", cat.getCategoryId(),
                        "categoryName", cat.getCategoryName(),
                        "categoryCode", cat.getCategoryCode()))
                .toList();

        sendSuccess(response, Map.of(
                "categories", categoryData,
                "count", categoryData.size()));
    }

    private void handleGetCategory(String categoryIdStr, HttpServletResponse response) throws IOException {
        try {
            int categoryId = Integer.parseInt(categoryIdStr);
            Category category = categoryRepository.findById(categoryId).orElse(null);

            if (category == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Category not found");
                return;
            }

            sendSuccess(response, Map.of(
                    "categoryId", category.getCategoryId(),
                    "categoryName", category.getCategoryName(),
                    "categoryCode", category.getCategoryCode()));
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID");
        }
    }

    private void handleGetSubcategories(String categoryIdStr, HttpServletResponse response) throws IOException {
        try {
            int categoryId = Integer.parseInt(categoryIdStr);
            List<Subcategory> subcategories = subcategoryRepository.findByCategoryId(categoryId);

            List<Map<String, Object>> subcategoryData = subcategories.stream()
                    .map(sub -> Map.<String, Object>of(
                            "subcategoryId", sub.getSubcategoryId(),
                            "subcategoryName", sub.getSubcategoryName(),
                            "subcategoryCode", sub.getSubcategoryCode(),
                            "categoryId", sub.getCategoryId()))
                    .toList();

            sendSuccess(response, Map.of(
                    "subcategories", subcategoryData,
                    "count", subcategoryData.size(),
                    "categoryId", categoryId));
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID");
        }
    }
}
