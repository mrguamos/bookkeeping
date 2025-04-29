package com.cubeia.bookkeeping.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferInput(
  @NotNull
  @Schema(description = "Source Wallet ID", example = "550e8400-e29b-41d4-a716-446655440000")
  UUID fromId,
  @NotNull
  @Schema(description = "Destination Wallet ID", example = "550e8400-e29b-41d4-a716-446655440000")
  UUID toId,
  @NotNull
  @Min(1)
  @Schema(description = "Amount to transfer", example = "100.00")
  BigDecimal amount
) {

}
