package com.cubeia.bookkeeping.wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferOutput(
  UUID transactionId,
  BigDecimal amount,
  BigDecimal newBalance
) {

}
