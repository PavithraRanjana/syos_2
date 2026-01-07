package com.syos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple service registry for dependency injection.
 * Replaces the Singleton pattern from CLI with a centralized service locator.
 *
 * Thread-safe implementation using ConcurrentHashMap.
 */
public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    private static final Map<String, Object> namedServices = new ConcurrentHashMap<>();

    private ServiceRegistry() {
        // Prevent instantiation
    }

    /**
     * Registers a service implementation for an interface.
     */
    public static <T> void register(Class<T> serviceClass, T implementation) {
        if (serviceClass == null || implementation == null) {
            throw new IllegalArgumentException("Service class and implementation cannot be null");
        }
        services.put(serviceClass, implementation);
        logger.debug("Registered service: {}", serviceClass.getSimpleName());
    }

    /**
     * Registers a service with a specific name.
     */
    public static void register(String name, Object implementation) {
        if (name == null || name.isEmpty() || implementation == null) {
            throw new IllegalArgumentException("Service name and implementation cannot be null");
        }
        namedServices.put(name, implementation);
        logger.debug("Registered named service: {}", name);
    }

    /**
     * Gets a service implementation by interface type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        T service = (T) services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException(
                "Service not registered: " + serviceClass.getSimpleName()
            );
        }
        return service;
    }

    /**
     * Gets a service implementation by interface type, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrNull(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }

    /**
     * Gets a named service.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name, Class<T> expectedType) {
        Object service = namedServices.get(name);
        if (service == null) {
            throw new IllegalStateException("Named service not registered: " + name);
        }
        if (!expectedType.isInstance(service)) {
            throw new IllegalStateException(
                "Named service '" + name + "' is not of expected type: " + expectedType.getSimpleName()
            );
        }
        return (T) service;
    }

    /**
     * Checks if a service is registered.
     */
    public static boolean isRegistered(Class<?> serviceClass) {
        return services.containsKey(serviceClass);
    }

    /**
     * Checks if a named service is registered.
     */
    public static boolean isRegistered(String name) {
        return namedServices.containsKey(name);
    }

    /**
     * Unregisters a service.
     */
    public static void unregister(Class<?> serviceClass) {
        services.remove(serviceClass);
        logger.debug("Unregistered service: {}", serviceClass.getSimpleName());
    }

    /**
     * Unregisters a named service.
     */
    public static void unregister(String name) {
        namedServices.remove(name);
        logger.debug("Unregistered named service: {}", name);
    }

    /**
     * Clears all registered services.
     * Used for testing or shutdown.
     */
    public static void clearAll() {
        services.clear();
        namedServices.clear();
        logger.info("Cleared all registered services");
    }

    /**
     * Returns the count of registered services.
     */
    public static int getServiceCount() {
        return services.size() + namedServices.size();
    }
}
