package com.syos.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JSON serialization/deserialization using Jackson.
 * Thread-safe singleton ObjectMapper.
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper mapper = createObjectMapper();

    private JsonUtil() {
        // Prevent instantiation
    }

    /**
     * Creates and configures the ObjectMapper.
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register Java 8 date/time module
        objectMapper.registerModule(new JavaTimeModule());

        // Don't write dates as timestamps
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Don't fail on unknown properties
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Indent output for readability
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return objectMapper;
    }

    /**
     * Converts an object to JSON string.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to JSON: {}", e.getMessage());
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    /**
     * Converts an object to compact JSON string (no indentation).
     */
    public static String toJsonCompact(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return mapper.writer().without(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to JSON: {}", e.getMessage());
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    /**
     * Parses a JSON string to an object of the specified class.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON parsing error", e);
        }
    }

    /**
     * Parses a JSON string to an object, returning null on error.
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            logger.warn("Failed to parse JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a string is valid JSON.
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            mapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Gets the shared ObjectMapper instance for advanced usage.
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}
