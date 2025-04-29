package com.cubeia.bookkeeping.ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Ledger(
  UUID id,
  UUID transactionId,
  UUID walletId,
  BigDecimal amount,
  BigDecimal runningBalance,
  Instant createdAt
) {


  public static final class LedgerBuilder {

    private UUID id;
    private UUID transactionId;
    private UUID walletId;
    private BigDecimal amount;
    private BigDecimal runningBalance;
    private Instant createdAt;

    public LedgerBuilder() {
    }

    public LedgerBuilder(Ledger other) {
      this.id = other.id();
      this.transactionId = other.transactionId();
      this.walletId = other.walletId();
      this.amount = other.amount();
      this.runningBalance = other.runningBalance();
      this.createdAt = other.createdAt();
    }

    public static LedgerBuilder aLedger() {
      return new LedgerBuilder();
    }

    public LedgerBuilder id(UUID id) {
      this.id = id;
      return this;
    }

    public LedgerBuilder transactionId(UUID transactionId) {
      this.transactionId = transactionId;
      return this;
    }

    public LedgerBuilder walletId(UUID walletId) {
      this.walletId = walletId;
      return this;
    }

    public LedgerBuilder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public LedgerBuilder runningBalance(BigDecimal runningBalance) {
      this.runningBalance = runningBalance;
      return this;
    }

    public LedgerBuilder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Ledger build() {
      return new Ledger(id, transactionId, walletId, amount, runningBalance, createdAt);
    }
  }
}
