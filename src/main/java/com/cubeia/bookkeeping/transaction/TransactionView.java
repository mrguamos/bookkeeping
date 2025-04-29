package com.cubeia.bookkeeping.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionView(
  UUID id,
  UUID fromId,
  UUID toId,
  BigDecimal amount,
  BigDecimal runningBalance,
  Instant createdAt
) {

}
