package com.cubeia.bookkeeping.wallet;

import com.cubeia.bookkeeping.api.Page;
import com.fasterxml.uuid.Generators;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
public class WalletController {

  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @PostMapping
  @Operation(description = "Create a new wallet", summary = "Create a new wallet")
  public CreateWalletOutput createWallet(@RequestBody @Valid CreateWalletInput input) {
    var id = walletService.createWallet(
      new Wallet.WalletBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .email(input.email())
        .balance(input.amount())
        .build()
    );
    return new CreateWalletOutput(id);
  }

  @GetMapping
  @Operation(description = "Get all wallets", summary = "Get all wallets")
  public List<Wallet> getWallets(
    @Valid Page page
  ) {
    return walletService.getWallets(page.limit(), page.offset());
  }

  @GetMapping("/{walletId}/balance")
  @Operation(description = "Get wallet balance by ID", summary = "Get wallet balance by ID")
  public WalletBalance getWalletBalance(@PathVariable UUID walletId) {
    var wallet = walletService.getWalletById(walletId, false);
    return new WalletBalance(walletId, wallet.balance());
  }


}
