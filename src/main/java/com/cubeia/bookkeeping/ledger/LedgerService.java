package com.cubeia.bookkeeping.ledger;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LedgerService {

  private final LedgerRepository ledgerRepository;

  public LedgerService(LedgerRepository ledgerRepository) {
    this.ledgerRepository = ledgerRepository;
  }

  public void createLedgerEntry(Ledger ledger) {
    ledgerRepository.createLedgerEntry(ledger);
  }

  @Transactional(readOnly = true)
  public List<Ledger> getLedgerEntriesByWalletId(UUID walletId, Integer limit, Integer offset) {
    return ledgerRepository.getLedgerEntriesByWalletId(walletId, limit, offset);
  }

}
