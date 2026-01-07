package com.syos.web.listener;

import com.syos.config.DataSourceConfig;
import com.syos.config.ServiceRegistry;
import com.syos.config.ThreadPoolConfig;
import com.syos.repository.impl.*;
import com.syos.repository.interfaces.*;
import com.syos.service.impl.*;
import com.syos.service.interfaces.*;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Application lifecycle listener.
 * Initializes and cleans up application resources.
 */
@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("==============================================");
        logger.info("SYOS Retail Management System Starting...");
        logger.info("==============================================");

        try {
            // Initialize DataSource
            logger.info("Initializing HikariCP DataSource...");
            DataSourceConfig.getDataSource();
            logger.info("DataSource initialized successfully");

            // Initialize Thread Pools
            logger.info("Initializing Thread Pools...");
            ThreadPoolConfig.getApiThreadPool();
            ThreadPoolConfig.getInventoryThreadPool();
            ThreadPoolConfig.getBackgroundTaskExecutor();
            logger.info("Thread pools initialized successfully");

            // Initialize Service Registry and register services
            logger.info("Initializing Service Registry...");
            initializeServices();
            logger.info("Service Registry initialized successfully");

            // Store context attributes
            sce.getServletContext().setAttribute("appName", "SYOS Retail Management System");
            sce.getServletContext().setAttribute("appVersion", "2.0.0");

            logger.info("==============================================");
            logger.info("SYOS Application Started Successfully!");
            logger.info("==============================================");

        } catch (Exception e) {
            logger.error("Failed to initialize application: {}", e.getMessage(), e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("==============================================");
        logger.info("SYOS Application Shutting Down...");
        logger.info("==============================================");

        try {
            // Shutdown thread pools
            logger.info("Shutting down Thread Pools...");
            ThreadPoolConfig.shutdownAll();

            // Close DataSource
            logger.info("Closing DataSource...");
            DataSourceConfig.closeDataSource();

            // Clear Service Registry
            logger.info("Clearing Service Registry...");
            ServiceRegistry.clearAll();

            logger.info("==============================================");
            logger.info("SYOS Application Shut Down Successfully");
            logger.info("==============================================");

        } catch (Exception e) {
            logger.error("Error during application shutdown: {}", e.getMessage(), e);
        }
    }

    /**
     * Initializes all services and registers them with the ServiceRegistry.
     */
    private void initializeServices() {
        DataSource dataSource = DataSourceConfig.getDataSource();

        // ==================== Register Repositories ====================
        logger.info("Registering repositories...");

        // Catalog repositories
        ProductRepository productRepository = new ProductRepositoryImpl(dataSource);
        ServiceRegistry.register(ProductRepository.class, productRepository);

        CategoryRepository categoryRepository = new CategoryRepositoryImpl(dataSource);
        ServiceRegistry.register(CategoryRepository.class, categoryRepository);

        SubcategoryRepository subcategoryRepository = new SubcategoryRepositoryImpl(dataSource);
        ServiceRegistry.register(SubcategoryRepository.class, subcategoryRepository);

        BrandRepository brandRepository = new BrandRepositoryImpl(dataSource);
        ServiceRegistry.register(BrandRepository.class, brandRepository);

        // Inventory repositories
        MainInventoryRepository mainInventoryRepository = new MainInventoryRepositoryImpl(dataSource);
        ServiceRegistry.register(MainInventoryRepository.class, mainInventoryRepository);

        PhysicalStoreInventoryRepository physicalStoreRepository = new PhysicalStoreInventoryRepositoryImpl(dataSource);
        ServiceRegistry.register(PhysicalStoreInventoryRepository.class, physicalStoreRepository);

        OnlineStoreInventoryRepository onlineStoreRepository = new OnlineStoreInventoryRepositoryImpl(dataSource);
        ServiceRegistry.register(OnlineStoreInventoryRepository.class, onlineStoreRepository);

        InventoryTransactionRepository transactionRepository = new InventoryTransactionRepositoryImpl(dataSource);
        ServiceRegistry.register(InventoryTransactionRepository.class, transactionRepository);

        // Billing repositories
        BillRepository billRepository = new BillRepositoryImpl(dataSource);
        ServiceRegistry.register(BillRepository.class, billRepository);

        BillItemRepository billItemRepository = new BillItemRepositoryImpl(dataSource);
        ServiceRegistry.register(BillItemRepository.class, billItemRepository);

        // Customer repository
        CustomerRepository customerRepository = new CustomerRepositoryImpl(dataSource);
        ServiceRegistry.register(CustomerRepository.class, customerRepository);

        logger.info("Repositories registered: {}", ServiceRegistry.getServiceCount());

        // ==================== Register Services ====================
        logger.info("Registering services...");

        // Product service
        ProductService productService = new ProductServiceImpl(productRepository);
        ServiceRegistry.register(ProductService.class, productService);

        // Inventory service
        InventoryService inventoryService = new InventoryServiceImpl(mainInventoryRepository, productRepository);
        ServiceRegistry.register(InventoryService.class, inventoryService);

        // Store inventory service
        StoreInventoryService storeInventoryService = new StoreInventoryServiceImpl(
            physicalStoreRepository,
            onlineStoreRepository,
            mainInventoryRepository,
            transactionRepository,
            productRepository
        );
        ServiceRegistry.register(StoreInventoryService.class, storeInventoryService);

        // Billing service
        BillingService billingService = new BillingServiceImpl(
            billRepository,
            billItemRepository,
            productRepository,
            storeInventoryService,
            transactionRepository
        );
        ServiceRegistry.register(BillingService.class, billingService);

        // Customer service
        CustomerService customerService = new CustomerServiceImpl(customerRepository);
        ServiceRegistry.register(CustomerService.class, customerService);

        // Report service
        ReportService reportService = new ReportServiceImpl(
            billRepository,
            billItemRepository,
            mainInventoryRepository,
            physicalStoreRepository,
            onlineStoreRepository
        );
        ServiceRegistry.register(ReportService.class, reportService);

        logger.info("Services registered. Total: {}", ServiceRegistry.getServiceCount());
    }
}
