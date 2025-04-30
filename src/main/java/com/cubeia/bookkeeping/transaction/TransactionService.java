package com.cubeia.bookkeeping.transaction;

import com.cubeia.bookkeeping.exception.InsufficientFundsException;
import com.cubeia.bookkeeping.exception.SameAccountTransferException;
import com.cubeia.bookkeeping.ledger.Ledger;
import com.cubeia.bookkeeping.ledger.LedgerService;
import com.cubeia.bookkeeping.wallet.WalletService;
import com.fasterxml.uuid.Generators;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {

  private final LedgerService ledgerService;
  private final WalletService walletService;
  private final TransactionRepository transactionRepository;

  public TransactionService(LedgerService ledgerService, WalletService walletService,
    TransactionRepository transactionRepository) {
    this.ledgerService = ledgerService;
    this.walletService = walletService;
    this.transactionRepository = transactionRepository;
  }

  public TransactionResult createTransaction(Transaction transaction) {
    if (transaction.fromId().equals(transaction.toId())) {
      throw new SameAccountTransferException();
    }

    var sourceWallet = walletService.getWalletById(transaction.fromId(), true);
    if (sourceWallet.balance().compareTo(transaction.amount()) < 0) {
      throw new InsufficientFundsException(
        "Insufficient funds"
      );
    }

    //check if destination wallet exists
    walletService.getWalletById(transaction.toId(), false);

    var sourceBalance = walletService.adjustBalance(transaction.fromId(),
      transaction.amount().negate()
    );

    var destinationBalance = walletService.adjustBalance(transaction.toId(),
      transaction.amount()
    );

    var transactionId = transactionRepository.createTransaction(transaction);

    //entry for debit

    ledgerService.createLedgerEntry(
      new Ledger.LedgerBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .transactionId(transactionId)
        .amount(transaction.amount().negate())
        .runningBalance(sourceBalance)
        .walletId(transaction.fromId())
        .build()
    );

    //entry for credit

    ledgerService.createLedgerEntry(
      new Ledger.LedgerBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .transactionId(transactionId)
        .amount(transaction.amount())
        .runningBalance(destinationBalance)
        .walletId(transaction.toId())
        .build()
    );

    return new TransactionResult(
      transactionId,
      sourceBalance
    );
  }

  @Transactional(readOnly = true)
  public List<TransactionView> getTransactionByWalletId(UUID walletId, Integer limit,
    Integer offset) {
    return transactionRepository.getTransactionByWalletId(walletId, limit, offset);
  }

}
