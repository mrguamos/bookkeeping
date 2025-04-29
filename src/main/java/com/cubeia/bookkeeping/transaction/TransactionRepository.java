package com.cubeia.bookkeeping.transaction;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository {

  private static final RowMapper<TransactionView> TRANSACTION_VIEW_ROW_MAPPER = (rs, rowNum) -> new TransactionView(
    rs.getObject("id", UUID.class),
    rs.getObject("from_id", UUID.class),
    rs.getObject("to_id", UUID.class),
    rs.getBigDecimal("amount"),
    rs.getBigDecimal("running_balance"),
    rs.getTimestamp("created_at").toInstant()
  );
  private final JdbcClient jdbcClient;

  public TransactionRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public UUID createTransaction(Transaction transaction) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql = "INSERT INTO txn.transaction (id, from_id, to_id, amount) VALUES (?, ?, ?, ?)";
    jdbcClient.sql(sql)
      .param(transaction.id())
      .param(transaction.fromId())
      .param(transaction.toId())
      .param(transaction.amount())
      .update(
        keyHolder, "id"
      );
    return keyHolder.getKeyAs(UUID.class);
  }

  public List<TransactionView> getTransactionByWalletId(UUID walletId, Integer limit,
    Integer offset) {
    if (limit == null || limit <= 0) {
      limit = 10;
    }
    if (offset == null || offset < 0) {
      offset = 0;
    }
    String sql =
      "SELECT t.id, t.from_id , t.to_id, t.created_at, l.amount, l.running_balance FROM txn.transaction t "
        + "INNER JOIN gnl.ledger l "
        + "ON l.transaction_id = t.id "
        + "WHERE l.wallet_id = ? "
        + "ORDER BY t.created_at DESC LIMIT ? OFFSET ?";
    return jdbcClient.sql(sql)
      .param(walletId)
      .param(limit)
      .param(offset)
      .query(TRANSACTION_VIEW_ROW_MAPPER)
      .list();
  }

}
