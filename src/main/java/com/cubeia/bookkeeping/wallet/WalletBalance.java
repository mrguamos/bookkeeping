package com.cubeia.bookkeeping.wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletBalance(
  UUID walletId,
  BigDecimal balance
) {

}
