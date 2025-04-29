package com.cubeia.bookkeeping.wallet;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class WalletControllerInputValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createWalletInput_WithValidData_ShouldPassValidation() {
        CreateWalletInput input = new CreateWalletInput("test@example.com", new BigDecimal("100"));
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertTrue(violations.isEmpty());
    }

    @Test
    void createWalletInput_WithInvalidEmail_ShouldFailValidation() {
        CreateWalletInput input = new CreateWalletInput("invalid-email", new BigDecimal("100"));
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be a well-formed email address", violations.iterator().next().getMessage());
    }

    @Test
    void createWalletInput_WithNullEmail_ShouldFailValidation() {
        CreateWalletInput input = new CreateWalletInput(null, new BigDecimal("100"));
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void createWalletInput_WithEmptyEmail_ShouldFailValidation() {
        CreateWalletInput input = new CreateWalletInput("", new BigDecimal("100"));
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void createWalletInput_WithNullAmount_ShouldFailValidation() {
        CreateWalletInput input = new CreateWalletInput("test@example.com", null);
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void createWalletInput_WithNegativeAmount_ShouldFailValidation() {
        CreateWalletInput input = new CreateWalletInput("test@example.com", new BigDecimal("-100"));
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 1", violations.iterator().next().getMessage());
    }

    @Test
    void createWalletInput_WithZeroAmount_ShouldFailValidation() {
        CreateWalletInput input = new CreateWalletInput("test@example.com", BigDecimal.ZERO);
        Set<ConstraintViolation<CreateWalletInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 1", violations.iterator().next().getMessage());
    }

    @Test
    void transferInput_WithValidData_ShouldPassValidation() {
        TransferInput input = new TransferInput(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                new BigDecimal("100")
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertTrue(violations.isEmpty());
    }

    @Test
    void transferInput_WithNullFromId_ShouldFailValidation() {
        TransferInput input = new TransferInput(
                null,
                java.util.UUID.randomUUID(),
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
                java.util.UUID.randomUUID(),
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
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
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
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
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
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                BigDecimal.ZERO
        );
        Set<ConstraintViolation<TransferInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 1", violations.iterator().next().getMessage());
    }
} 