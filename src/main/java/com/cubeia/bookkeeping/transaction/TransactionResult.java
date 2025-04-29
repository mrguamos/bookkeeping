package com.cubeia.bookkeeping.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResult(
  UUID transactionId,
  BigDecimal newBalance
) {

}
