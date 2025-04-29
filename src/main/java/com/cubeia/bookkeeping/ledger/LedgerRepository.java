package com.cubeia.bookkeeping.ledger;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LedgerRepository {

  private static final RowMapper<Ledger> LEDGER_ROW_MAPPER = (rs, rowNum) -> new Ledger.LedgerBuilder()
    .id(rs.getObject("id", UUID.class))
    .transactionId(rs.getObject("transaction_id", UUID.class))
    .walletId(rs.getObject("wallet_id", UUID.class))
    .amount(rs.getBigDecimal("amount"))
    .createdAt(rs.getTimestamp("created_at").toInstant())
    .build();
  private final JdbcClient jdbcClient;

  public LedgerRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void createLedgerEntry(Ledger ledger) {
    String sql = "INSERT INTO gnl.ledger (id, transaction_id, wallet_id, amount, running_balance) VALUES (?, ?, ?, ?, ?)";
    jdbcClient.sql(sql)
      .param(ledger.id())
      .param(ledger.transactionId())
      .param(ledger.walletId())
      .param(ledger.amount())
      .param(ledger.runningBalance())
      .update();
  }

  public List<Ledger> getLedgerEntriesByWalletId(UUID walletId, Integer limit, Integer offset) {
    if (limit == null || limit <= 0) {
      limit = 10;
    }
    if (offset == null || offset < 0) {
      offset = 0;
    }
    String sql = "SELECT id, transaction_id, wallet_id, amount, created_at FROM gnl.ledger WHERE wallet_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
    return jdbcClient.sql(sql)
      .param(walletId)
      .param(limit)
      .param(offset)
      .query(LEDGER_ROW_MAPPER)
      .list();
  }

}
