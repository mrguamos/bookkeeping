package com.cubeia.bookkeeping.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Wallet(
  UUID id,
  String email,
  BigDecimal balance,
  Instant createdAt
) {


  public static final class WalletBuilder {

    private UUID id;
    private String email;
    private BigDecimal balance;
    private Instant createdAt;

    public WalletBuilder() {
    }

    public WalletBuilder(Wallet other) {
      this.id = other.id();
      this.email = other.email();
      this.balance = other.balance();
      this.createdAt = other.createdAt();
    }

    public static WalletBuilder aWallet() {
      return new WalletBuilder();
    }

    public WalletBuilder id(UUID id) {
      this.id = id;
      return this;
    }

    public WalletBuilder email(String email) {
      this.email = email;
      return this;
    }

    public WalletBuilder balance(BigDecimal balance) {
      this.balance = balance;
      return this;
    }

    public WalletBuilder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Wallet build() {
      return new Wallet(id, email, balance, createdAt);
    }
  }
}
