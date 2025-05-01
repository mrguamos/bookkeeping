package com.cubeia.bookkeeping.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cubeia.bookkeeping.BaseIntegrationTest;
import com.cubeia.bookkeeping.wallet.CreateWalletInput;
import com.cubeia.bookkeeping.wallet.CreateWalletOutput;
import com.cubeia.bookkeeping.wallet.TransferInput;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class TransactionConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateUniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void concurrentTransfers_ShouldMaintainBalanceConsistency() throws Exception {
        // Create source wallet with initial balance
        String sourceEmail = generateUniqueEmail();
        CreateWalletInput sourceInput = new CreateWalletInput(sourceEmail, new BigDecimal("1000"));
        String sourceResponse = mockMvc.perform(post("/wallets")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(sourceInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput sourceOutput = objectMapper.readValue(sourceResponse, CreateWalletOutput.class);

        // Create target wallet
        String targetEmail = generateUniqueEmail();
        CreateWalletInput targetInput = new CreateWalletInput(targetEmail, new BigDecimal("1"));
        String targetResponse = mockMvc.perform(post("/wallets")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(targetInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput targetOutput = objectMapper.readValue(targetResponse, CreateWalletOutput.class);

        // Number of concurrent transfers
        int numTransfers = 10;
        // Amount to transfer in each transaction
        BigDecimal transferAmount = new BigDecimal("10");
        // Expected final balance
        BigDecimal expectedFinalBalance = new BigDecimal("1").add(transferAmount.multiply(new BigDecimal(numTransfers)));

        // Create executor service for concurrent execution
        ExecutorService executorService = Executors.newFixedThreadPool(numTransfers);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Submit concurrent transfer requests
        for (int i = 0; i < numTransfers; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    TransferInput transferInput = new TransferInput(
                            sourceOutput.id(),
                            targetOutput.id(),
                            transferAmount
                    );
                    mockMvc.perform(post("/transactions/transfer")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(transferInput)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").isNotEmpty());
                } catch (Exception e) {
                    throw new RuntimeException("Transfer failed", e);
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all transfers to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);

        // Verify final balance
        mockMvc.perform(get("/wallets/{id}/balance", targetOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(expectedFinalBalance));

        // Verify source wallet balance
        BigDecimal expectedSourceBalance = new BigDecimal("1000")
                .subtract(transferAmount.multiply(new BigDecimal(numTransfers)));
        mockMvc.perform(get("/wallets/{id}/balance", sourceOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(expectedSourceBalance));

        // Verify transaction count
        mockMvc.perform(get("/transactions/{walletId}?limit=11", targetOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(numTransfers + 1));

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void bidirectionalTransfers_ShouldMaintainBalanceConsistency() throws Exception {
        // Create first wallet with initial balance
        String wallet1Email = generateUniqueEmail();
        CreateWalletInput wallet1Input = new CreateWalletInput(wallet1Email, new BigDecimal("1000"));
        String wallet1Response = mockMvc.perform(post("/wallets")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(wallet1Input)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput wallet1Output = objectMapper.readValue(wallet1Response, CreateWalletOutput.class);

        // Create second wallet with initial balance
        String wallet2Email = generateUniqueEmail();
        CreateWalletInput wallet2Input = new CreateWalletInput(wallet2Email, new BigDecimal("1000"));
        String wallet2Response = mockMvc.perform(post("/wallets")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(wallet2Input)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput wallet2Output = objectMapper.readValue(wallet2Response, CreateWalletOutput.class);

        // Number of concurrent transfer pairs
        int numTransferPairs = 50;
        // Amount to transfer in each direction
        BigDecimal transferAmount = new BigDecimal("10");

        // Create executor service for concurrent execution
        ExecutorService executorService = Executors.newFixedThreadPool(numTransferPairs * 2);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Submit concurrent bidirectional transfer requests
        for (int i = 0; i < numTransferPairs; i++) {
            // Transfer from wallet1 to wallet2
            CompletableFuture<Void> forward = CompletableFuture.runAsync(() -> {
                try {
                    TransferInput transferInput = new TransferInput(
                            wallet1Output.id(),
                            wallet2Output.id(),
                            transferAmount
                    );
                    mockMvc.perform(post("/transactions/transfer")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(transferInput)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").isNotEmpty());
                } catch (Exception e) {
                    throw new RuntimeException("Forward transfer failed", e);
                }
            }, executorService);
            
            // Transfer from wallet2 to wallet1
            CompletableFuture<Void> reverse = CompletableFuture.runAsync(() -> {
                try {
                    TransferInput transferInput = new TransferInput(
                            wallet2Output.id(),
                            wallet1Output.id(),
                            transferAmount
                    );
                    mockMvc.perform(post("/transactions/transfer")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(transferInput)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").isNotEmpty());
                } catch (Exception e) {
                    throw new RuntimeException("Reverse transfer failed", e);
                }
            }, executorService);

            futures.add(forward);
            futures.add(reverse);
        }

        // Wait for all transfers to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);

        // Since we're doing equal transfers in both directions, final balances should equal initial balances
        mockMvc.perform(get("/wallets/{id}/balance", wallet1Output.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("1000"));

        mockMvc.perform(get("/wallets/{id}/balance", wallet2Output.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("1000"));

        // Verify transaction counts - each wallet should have numTransferPairs * 2 transactions
        // (initial deposit + transfers in both directions)
        mockMvc.perform(get("/transactions/{walletId}?limit=101", wallet1Output.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(numTransferPairs * 2 + 1));

        mockMvc.perform(get("/transactions/{walletId}?limit=101", wallet2Output.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(numTransferPairs * 2 + 1));

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }
} 