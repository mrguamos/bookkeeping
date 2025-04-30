package com.cubeia.bookkeeping.transaction;

import com.cubeia.bookkeeping.api.Page;
import com.cubeia.bookkeeping.wallet.TransferInput;
import com.cubeia.bookkeeping.wallet.TransferOutput;
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
@RequestMapping("/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping("/transfer")
  @Operation(
    description = "Transfer money between wallets",
    summary = "Transfer money between wallets"
  )
  public TransferOutput transfer(@RequestBody @Valid TransferInput input) {
    var transactionResult = transactionService.createTransaction(
      new Transaction.TransactionBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .fromId(input.fromId())
        .toId(input.toId())
        .amount(input.amount())
        .build()
    );
    return new TransferOutput(
      transactionResult.transactionId(),
      input.amount(),
      transactionResult.newBalance()
    );
  }

  @GetMapping("/{walletId}")
  @Operation(
    description = "Get transactions by wallet ID",
    summary = "Get transactions by wallet ID"
  )
  public List<TransactionView> getTransactionByWalletId(
    @PathVariable UUID walletId,
    @Valid Page page
  ) {
    return transactionService.getTransactionByWalletId(
      walletId,
      page.limit(),
      page.offset()
    );
  }

}
