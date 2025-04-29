package com.cubeia.bookkeeping.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
  UUID id,
  UUID fromId,
  UUID toId,
  BigDecimal amount,
  Instant createdAt
) {


  public static final class TransactionBuilder {

    private UUID id;
    private UUID fromId;
    private UUID toId;
    private BigDecimal amount;
    private Instant createdAt;

    public TransactionBuilder() {
    }

    public TransactionBuilder(Transaction other) {
      this.id = other.id();
      this.fromId = other.fromId();
      this.toId = other.toId();
      this.amount = other.amount();
      this.createdAt = other.createdAt();
    }

    public static TransactionBuilder aTransaction() {
      return new TransactionBuilder();
    }

    public TransactionBuilder id(UUID id) {
      this.id = id;
      return this;
    }

    public TransactionBuilder fromId(UUID fromId) {
      this.fromId = fromId;
      return this;
    }

    public TransactionBuilder toId(UUID toId) {
      this.toId = toId;
      return this;
    }

    public TransactionBuilder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public TransactionBuilder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Transaction build() {
      return new Transaction(id, fromId, toId, amount, createdAt);
    }
  }
}
