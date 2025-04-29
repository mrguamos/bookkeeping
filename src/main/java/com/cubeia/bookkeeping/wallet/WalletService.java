package com.cubeia.bookkeeping.wallet;

import com.cubeia.bookkeeping.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WalletService {

  private final WalletRepository walletRepository;

  public WalletService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public UUID createWallet(Wallet wallet) {
    if (walletRepository.getWalletByEmail(wallet.email()).isPresent()) {
      throw new IllegalArgumentException("Wallet with email already exists");
    }
    return walletRepository.createWallet(wallet);
  }

  @Transactional(readOnly = true)
  public List<Wallet> getWallets(Integer limit, Integer offset) {
    return walletRepository.getWallets(limit, offset);
  }

  @Transactional(readOnly = true)
  public Wallet getWalletById(UUID id, boolean rowLock) {
    var wallet = walletRepository.getWalletById(id, rowLock);
    return wallet.orElseThrow(
      () -> new NotFoundException("Wallet not found: " + id)
    );
  }

  public BigDecimal adjustBalance(UUID id, BigDecimal amount) {
    return walletRepository.adjustBalance(id, amount);
  }

}
