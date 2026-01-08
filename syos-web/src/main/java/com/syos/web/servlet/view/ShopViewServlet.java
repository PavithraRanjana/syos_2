package com.syos.web.servlet.view;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet for serving online shop views.
 * Handles routing for shop, cart, checkout, and orders pages.
 */
@WebServlet(urlPatterns = {"/shop", "/cart", "/checkout", "/orders"})
public class ShopViewServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ShopViewServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        logger.debug("Shop view request: {}", path);

        String viewPath = switch (path) {
            case "/shop" -> "/WEB-INF/views/shop/index.jsp";
            case "/cart" -> "/WEB-INF/views/shop/cart.jsp";
            case "/checkout" -> "/WEB-INF/views/shop/checkout.jsp";
            case "/orders" -> "/WEB-INF/views/shop/orders.jsp";
            default -> "/WEB-INF/views/shop/index.jsp";
        };

        request.getRequestDispatcher(viewPath).forward(request, response);
    }
}
