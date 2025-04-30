package com.cubeia.bookkeeping.transaction;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {

  private final TransactionProcessor transactionProcessor;
  private final TransactionRepository transactionRepository;

  public TransactionService(
    TransactionProcessor transactionProcessor, TransactionRepository transactionRepository) {
    this.transactionProcessor = transactionProcessor;
    this.transactionRepository = transactionRepository;
  }

  public TransactionResult createTransaction(Transaction transaction) {
    return transactionProcessor.createTransaction(transaction, false);
  }

  @Transactional(readOnly = true)
  public List<TransactionView> getTransactionByWalletId(UUID walletId, Integer limit,
    Integer offset) {
    return transactionRepository.getTransactionByWalletId(walletId, limit, offset);
  }

}
