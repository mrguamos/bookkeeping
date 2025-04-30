package com.cubeia.bookkeeping.wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WalletRepository {

  private static final RowMapper<Wallet> WALLET_ROW_MAPPER = (rs, rowNum) -> new Wallet.WalletBuilder()
    .id(rs.getObject("id", UUID.class))
    .email(rs.getString("email"))
    .balance(rs.getBigDecimal("balance"))
    .createdAt(rs.getTimestamp("created_at").toInstant())
    .build();
  private static final RowMapper<BigDecimal> BALANCE_ROW_MAPPER = (rs, rowNum) -> rs.getBigDecimal(
    "balance");
  private final JdbcClient jdbcClient;

  public WalletRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public UUID createWallet(Wallet wallet) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql = "INSERT INTO mng.wallet (id, email, balance) VALUES (?, ?, ?)";
    jdbcClient.sql(sql)
      .param(wallet.id())
      .param(wallet.email())
      .param(wallet.balance())
      .update(
        keyHolder, "id"
      );
    return keyHolder.getKeyAs(UUID.class);
  }

  public List<Wallet> getWallets(Integer limit, Integer offset) {
    if (limit == null || limit <= 0) {
      limit = 10;
    }
    if (offset == null || offset < 0) {
      offset = 0;
    }
    String sql = "SELECT id, email, balance, created_at FROM mng.wallet ORDER BY created_at DESC LIMIT ? OFFSET ?";
    return jdbcClient.sql(sql)
      .param(limit)
      .param(offset)
      .query(WALLET_ROW_MAPPER)
      .list();
  }

  public Wallet getWalletById(UUID id, boolean rowLock) {
    String sql = "SELECT id, email, balance, created_at FROM mng.wallet WHERE id = ?"
      + (rowLock ? " FOR UPDATE" : "");
    return jdbcClient.sql(sql)
      .param(id)
      .query(WALLET_ROW_MAPPER)
      .optional().orElse(null);
  }

  public BigDecimal adjustBalance(UUID id, BigDecimal amount) {
    String sql = "UPDATE mng.wallet SET balance = balance + ? WHERE id = ? returning balance";
    return jdbcClient.sql(sql)
      .param(amount)
      .param(id)
      .query(BALANCE_ROW_MAPPER)
      .single();
  }

  public Optional<Wallet> getWalletByEmail(String email) {
    String sql = "SELECT id, email, balance, created_at FROM mng.wallet WHERE email = ?";
    return jdbcClient.sql(sql)
      .param(email)
      .query(WALLET_ROW_MAPPER)
      .optional();
  }

}
