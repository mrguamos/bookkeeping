package com.cubeia.bookkeeping.transaction;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cubeia.bookkeeping.wallet.TransferInput;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TransactionControllerInputValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void transferInput_WithValidData_ShouldPassValidation() {
        TransferInput input = new TransferInput(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100")
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertTrue(violations.isEmpty());
    }

    @Test
    void transferInput_WithNullFromId_ShouldFailValidation() {
        TransferInput input = new TransferInput(
                null,
                UUID.randomUUID(),
                new BigDecimal("100")
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void transferInput_WithNullToId_ShouldFailValidation() {
        TransferInput input = new TransferInput(
                UUID.randomUUID(),
                null,
                new BigDecimal("100")
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void transferInput_WithNullAmount_ShouldFailValidation() {
        TransferInput input = new TransferInput(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void transferInput_WithNegativeAmount_ShouldFailValidation() {
        TransferInput input = new TransferInput(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("-100")
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 1", violations.iterator().next().getMessage());
    }

    @Test
    void transferInput_WithZeroAmount_ShouldFailValidation() {
        TransferInput input = new TransferInput(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ZERO
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 1", violations.iterator().next().getMessage());
    }
} 