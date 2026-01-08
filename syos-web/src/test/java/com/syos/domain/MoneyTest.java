package com.syos.domain;

import com.syos.domain.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Money value object.
 */
class MoneyTest {

    @Nested
    @DisplayName("Construction tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create Money from BigDecimal")
        void shouldCreateFromBigDecimal() {
            Money money = new Money(BigDecimal.valueOf(100.50));
            assertEquals(new BigDecimal("100.50"), money.getAmount());
        }

        @Test
        @DisplayName("Should create Money from String")
        void shouldCreateFromString() {
            Money money = new Money("50.25");
            assertEquals(new BigDecimal("50.25"), money.getAmount());
        }

        @Test
        @DisplayName("Should create Money from double")
        void shouldCreateFromDouble() {
            Money money = new Money(75.99);
            assertEquals(new BigDecimal("75.99"), money.getAmount());
        }

        @Test
        @DisplayName("Should round to 2 decimal places")
        void shouldRoundToTwoDecimalPlaces() {
            Money money = new Money(BigDecimal.valueOf(100.555));
            assertEquals(new BigDecimal("100.56"), money.getAmount());
        }

        @Test
        @DisplayName("Should throw exception for null amount")
        void shouldThrowForNullAmount() {
            assertThrows(IllegalArgumentException.class, () -> new Money((BigDecimal) null));
        }

        @Test
        @DisplayName("Should throw exception for negative amount")
        void shouldThrowForNegativeAmount() {
            assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.valueOf(-10.00)));
        }
    }

    @Nested
    @DisplayName("Arithmetic tests")
    class ArithmeticTests {

        @Test
        @DisplayName("Should add two Money values")
        void shouldAddTwoMoneyValues() {
            Money a = new Money(100.00);
            Money b = new Money(50.00);
            Money result = a.add(b);
            assertEquals(new BigDecimal("150.00"), result.getAmount());
        }

        @Test
        @DisplayName("Should handle null addition")
        void shouldHandleNullAddition() {
            Money a = new Money(100.00);
            Money result = a.add(null);
            assertEquals(new BigDecimal("100.00"), result.getAmount());
        }

        @Test
        @DisplayName("Should subtract two Money values")
        void shouldSubtractTwoMoneyValues() {
            Money a = new Money(100.00);
            Money b = new Money(30.00);
            Money result = a.subtract(b);
            assertEquals(new BigDecimal("70.00"), result.getAmount());
        }

        @Test
        @DisplayName("Should throw exception for negative subtraction result")
        void shouldThrowForNegativeSubtractionResult() {
            Money a = new Money(50.00);
            Money b = new Money(100.00);
            assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
        }

        @Test
        @DisplayName("Should multiply by integer")
        void shouldMultiplyByInteger() {
            Money money = new Money(25.00);
            Money result = money.multiply(4);
            assertEquals(new BigDecimal("100.00"), result.getAmount());
        }

        @Test
        @DisplayName("Should throw exception for negative multiplier")
        void shouldThrowForNegativeMultiplier() {
            Money money = new Money(25.00);
            assertThrows(IllegalArgumentException.class, () -> money.multiply(-1));
        }

        @Test
        @DisplayName("Should multiply by BigDecimal")
        void shouldMultiplyByBigDecimal() {
            Money money = new Money(100.00);
            Money result = money.multiply(BigDecimal.valueOf(0.5));
            assertEquals(new BigDecimal("50.00"), result.getAmount());
        }
    }

    @Nested
    @DisplayName("Comparison tests")
    class ComparisonTests {

        @Test
        @DisplayName("Should identify greater than")
        void shouldIdentifyGreaterThan() {
            Money a = new Money(100.00);
            Money b = new Money(50.00);
            assertTrue(a.isGreaterThan(b));
            assertFalse(b.isGreaterThan(a));
        }

        @Test
        @DisplayName("Should identify greater than or equal")
        void shouldIdentifyGreaterThanOrEqual() {
            Money a = new Money(100.00);
            Money b = new Money(100.00);
            Money c = new Money(50.00);
            assertTrue(a.isGreaterThanOrEqual(b));
            assertTrue(a.isGreaterThanOrEqual(c));
            assertFalse(c.isGreaterThanOrEqual(a));
        }

        @Test
        @DisplayName("Should identify less than")
        void shouldIdentifyLessThan() {
            Money a = new Money(50.00);
            Money b = new Money(100.00);
            assertTrue(a.isLessThan(b));
            assertFalse(b.isLessThan(a));
        }

        @Test
        @DisplayName("Should identify zero")
        void shouldIdentifyZero() {
            assertTrue(Money.ZERO.isZero());
            assertFalse(new Money(1.00).isZero());
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal for same amount")
        void shouldBeEqualForSameAmount() {
            Money a = new Money(100.00);
            Money b = new Money(100.00);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different amounts")
        void shouldNotBeEqualForDifferentAmounts() {
            Money a = new Money(100.00);
            Money b = new Money(50.00);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Should be equal regardless of scale")
        void shouldBeEqualRegardlessOfScale() {
            Money a = new Money(new BigDecimal("100.00"));
            Money b = new Money(new BigDecimal("100"));
            assertEquals(a, b);
        }
    }

    @Nested
    @DisplayName("Formatting tests")
    class FormattingTests {

        @Test
        @DisplayName("Should format with currency symbol")
        void shouldFormatWithCurrencySymbol() {
            Money money = new Money(1234.56);
            String formatted = money.format();
            assertTrue(formatted.contains("Rs."));
            assertTrue(formatted.contains("1,234.56") || formatted.contains("1234.56"));
        }

        @Test
        @DisplayName("toString should return formatted value")
        void toStringShouldReturnFormattedValue() {
            Money money = new Money(100.00);
            assertTrue(money.toString().contains("Rs."));
        }
    }

    @Nested
    @DisplayName("ZERO constant tests")
    class ZeroConstantTests {

        @Test
        @DisplayName("ZERO should be zero value")
        void zeroShouldBeZeroValue() {
            assertEquals(BigDecimal.ZERO.setScale(2), Money.ZERO.getAmount());
        }

        @Test
        @DisplayName("ZERO should be immutable")
        void zeroShouldBeImmutable() {
            Money result = Money.ZERO.add(new Money(100.00));
            assertEquals(new BigDecimal("100.00"), result.getAmount());
            assertTrue(Money.ZERO.isZero());
        }
    }
}
