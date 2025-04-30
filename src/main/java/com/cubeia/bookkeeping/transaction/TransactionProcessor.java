package com.cubeia.bookkeeping.transaction;

import com.cubeia.bookkeeping.exception.InsufficientFundsException;
import com.cubeia.bookkeeping.exception.NotFoundException;
import com.cubeia.bookkeeping.exception.SameAccountTransferException;
import com.cubeia.bookkeeping.ledger.Ledger;
import com.cubeia.bookkeeping.ledger.LedgerRepository;
import com.cubeia.bookkeeping.wallet.WalletRepository;
import com.fasterxml.uuid.Generators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionProcessor {

  private final TransactionRepository transactionRepository;
  private final LedgerRepository ledgerRepository;
  private final WalletRepository walletRepository;

  public TransactionProcessor(TransactionRepository transactionRepository,
    LedgerRepository ledgerRepository, WalletRepository walletRepository) {
    this.transactionRepository = transactionRepository;
    this.ledgerRepository = ledgerRepository;
    this.walletRepository = walletRepository;
  }

  public TransactionResult createTransaction(Transaction transaction, boolean initial) {

    if (initial) {
      var transactionId = transactionRepository.createTransaction(transaction);
      ledgerRepository.createLedgerEntry(
        new Ledger.LedgerBuilder()
          .id(Generators.timeBasedEpochRandomGenerator().generate())
          .transactionId(transactionId)
          .amount(transaction.amount())
          .runningBalance(transaction.amount())
          .walletId(transaction.toId())
          .build()
      );
      return new TransactionResult(
        transactionId,
        transaction.amount()
      );
    }

    if (transaction.fromId().equals(transaction.toId())) {
      throw new SameAccountTransferException();
    }

    var sourceWallet = walletRepository.getWalletById(transaction.fromId(), true);
    if (sourceWallet == null) {
      throw new NotFoundException("Wallet not found: " + transaction.fromId());
    }

    if (sourceWallet.balance().compareTo(transaction.amount()) < 0) {
      throw new InsufficientFundsException(
        "Insufficient funds"
      );
    }

    var destinationWallet = walletRepository.getWalletById(transaction.toId(), false);
    if (destinationWallet == null) {
      throw new NotFoundException("Wallet not found: " + transaction.fromId());
    }

    var sourceBalance = walletRepository.adjustBalance(transaction.fromId(),
      transaction.amount().negate()
    );

    var destinationBalance = walletRepository.adjustBalance(transaction.toId(),
      transaction.amount()
    );

    var transactionId = transactionRepository.createTransaction(transaction);

    //entry for debit

    ledgerRepository.createLedgerEntry(
      new Ledger.LedgerBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .transactionId(transactionId)
        .amount(transaction.amount().negate())
        .runningBalance(sourceBalance)
        .walletId(transaction.fromId())
        .build()
    );

    //entry for credit

    ledgerRepository.createLedgerEntry(
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

}
