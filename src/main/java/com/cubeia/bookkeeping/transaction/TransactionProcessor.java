package com.cubeia.bookkeeping.transaction;

import com.cubeia.bookkeeping.exception.InsufficientFundsException;
import com.cubeia.bookkeeping.exception.NotFoundException;
import com.cubeia.bookkeeping.exception.SameAccountTransferException;
import com.cubeia.bookkeeping.ledger.Ledger;
import com.cubeia.bookkeeping.ledger.LedgerRepository;
import com.cubeia.bookkeeping.wallet.WalletRepository;
import com.fasterxml.uuid.Generators;
import java.util.UUID;
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

    // Lock wallets in consistent order based on UUID comparison
    UUID firstLockId, secondLockId;
    boolean isSourceFirst;

    if (transaction.fromId().compareTo(transaction.toId()) < 0) {
      firstLockId = transaction.fromId();
      secondLockId = transaction.toId();
      isSourceFirst = true;
    } else {
      firstLockId = transaction.toId();
      secondLockId = transaction.fromId();
      isSourceFirst = false;
    }

    // Get both wallets with locks in consistent order
    var firstWallet = walletRepository.getWalletById(firstLockId, true);
    if (firstWallet == null) {
      throw new NotFoundException("Wallet not found: " + firstLockId);
    }

    var secondWallet = walletRepository.getWalletById(secondLockId, true);
    if (secondWallet == null) {
      throw new NotFoundException("Wallet not found: " + secondLockId);
    }

    // Reference source and destination wallets based on the lock order
    var sourceWallet = isSourceFirst ? firstWallet : secondWallet;

    // Check balance
    if (sourceWallet.balance().compareTo(transaction.amount()) < 0) {
      throw new InsufficientFundsException("Insufficient funds");
    }

    // Perform balance updates
    var sourceBalance = walletRepository.adjustBalance(transaction.fromId(),
      transaction.amount().negate()
    );

    var destinationBalance = walletRepository.adjustBalance(transaction.toId(),
      transaction.amount()
    );

    // Create transaction record
    var transactionId = transactionRepository.createTransaction(transaction);

    // Create ledger entries
    ledgerRepository.createLedgerEntry(
      new Ledger.LedgerBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .transactionId(transactionId)
        .amount(transaction.amount().negate())
        .runningBalance(sourceBalance)
        .walletId(transaction.fromId())
        .build()
    );

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
