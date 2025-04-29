package com.cubeia.bookkeeping.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateWalletInput(
  @NotBlank @Email @Schema(description = "Email", example = "youremail@gmail.com") String email,
  @NotNull @Min(1) @Schema(description = "Initial Amount", example = "100.00") BigDecimal amount
) {

}
