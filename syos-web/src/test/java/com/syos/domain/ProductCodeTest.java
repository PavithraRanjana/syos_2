package com.syos.domain;

import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductCode value object.
 */
class ProductCodeTest {

    @Nested
    @DisplayName("Construction tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create valid product code")
        void shouldCreateValidProductCode() {
            ProductCode code = new ProductCode("BEV-SD-CC-001");
            assertEquals("BEV-SD-CC-001", code.getCode());
        }

        @Test
        @DisplayName("Should convert to uppercase")
        void shouldConvertToUppercase() {
            ProductCode code = new ProductCode("bev-sd-cc-001");
            assertEquals("BEV-SD-CC-001", code.getCode());
        }

        @Test
        @DisplayName("Should trim whitespace")
        void shouldTrimWhitespace() {
            ProductCode code = new ProductCode("  BEV-001  ");
            assertEquals("BEV-001", code.getCode());
        }

        @Test
        @DisplayName("Should throw exception for null code")
        void shouldThrowForNullCode() {
            assertThrows(IllegalArgumentException.class, () -> new ProductCode(null));
        }

        @Test
        @DisplayName("Should throw exception for empty code")
        void shouldThrowForEmptyCode() {
            assertThrows(IllegalArgumentException.class, () -> new ProductCode(""));
        }

        @Test
        @DisplayName("Should throw exception for whitespace only code")
        void shouldThrowForWhitespaceOnlyCode() {
            assertThrows(IllegalArgumentException.class, () -> new ProductCode("   "));
        }

        @Test
        @DisplayName("Should throw exception for code exceeding max length")
        void shouldThrowForCodeExceedingMaxLength() {
            String longCode = "A".repeat(20);
            assertThrows(IllegalArgumentException.class, () -> new ProductCode(longCode));
        }
    }

    @Nested
    @DisplayName("Category code extraction tests")
    class CategoryCodeTests {

        @Test
        @DisplayName("Should extract category code")
        void shouldExtractCategoryCode() {
            ProductCode code = new ProductCode("BEV-SD-CC-001");
            assertEquals("BE", code.getCategoryCode());
        }

        @Test
        @DisplayName("Should return full code if shorter than 2 chars")
        void shouldReturnFullCodeIfShorterThan2Chars() {
            ProductCode code = new ProductCode("A");
            assertEquals("A", code.getCategoryCode());
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal for same code")
        void shouldBeEqualForSameCode() {
            ProductCode a = new ProductCode("BEV-001");
            ProductCode b = new ProductCode("BEV-001");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Should be equal regardless of case")
        void shouldBeEqualRegardlessOfCase() {
            ProductCode a = new ProductCode("BEV-001");
            ProductCode b = new ProductCode("bev-001");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Should not be equal for different codes")
        void shouldNotBeEqualForDifferentCodes() {
            ProductCode a = new ProductCode("BEV-001");
            ProductCode b = new ProductCode("BEV-002");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ProductCode code = new ProductCode("BEV-001");
            assertNotEquals(null, code);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            ProductCode code = new ProductCode("BEV-001");
            assertNotEquals("BEV-001", code);
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should return code")
        void toStringShouldReturnCode() {
            ProductCode code = new ProductCode("BEV-001");
            assertEquals("BEV-001", code.toString());
        }
    }
}
