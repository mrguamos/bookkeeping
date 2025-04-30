package com.cubeia.bookkeeping.wallet;

import com.cubeia.bookkeeping.exception.NotFoundException;
import com.cubeia.bookkeeping.exception.UniqueException;
import com.cubeia.bookkeeping.transaction.Transaction;
import com.cubeia.bookkeeping.transaction.TransactionProcessor;
import com.fasterxml.uuid.Generators;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WalletService {

  private final WalletRepository walletRepository;
  private final TransactionProcessor transactionProcessor;

  public WalletService(WalletRepository walletRepository,
    TransactionProcessor transactionProcessor) {
    this.walletRepository = walletRepository;
    this.transactionProcessor = transactionProcessor;
  }

  public UUID createWallet(Wallet wallet) {
    if (walletRepository.getWalletByEmail(wallet.email()).isPresent()) {
      throw new UniqueException("Wallet with email already exists");
    }

    var id = walletRepository.createWallet(wallet);

    transactionProcessor.createTransaction(
      new Transaction.TransactionBuilder()
        .id(Generators.timeBasedEpochRandomGenerator().generate())
        .toId(id)
        .amount(wallet.balance())
        .build(),
      true
    );

    return id;
  }

  @Transactional(readOnly = true)
  public List<Wallet> getWallets(Integer limit, Integer offset) {
    return walletRepository.getWallets(limit, offset);
  }

  @Transactional(readOnly = true)
  public Wallet getWalletById(UUID id, boolean rowLock) {
    var wallet = walletRepository.getWalletById(id, rowLock);
    if (wallet == null) {
      throw new NotFoundException("Wallet not found: " + id);
    }
    return wallet;
  }

  public BigDecimal adjustBalance(UUID id, BigDecimal amount) {
    return walletRepository.adjustBalance(id, amount);
  }

}
