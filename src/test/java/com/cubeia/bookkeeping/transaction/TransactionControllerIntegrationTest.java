package com.cubeia.bookkeeping.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateUniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void transfer_ShouldCreateTransaction() throws Exception {
        // Create source wallet
        String sourceEmail = generateUniqueEmail();
        CreateWalletInput sourceInput = new CreateWalletInput(sourceEmail, new BigDecimal("100"));
        String sourceResponse = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sourceInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput sourceOutput = objectMapper.readValue(sourceResponse, CreateWalletOutput.class);

        // Create target wallet
        String targetEmail = generateUniqueEmail();
        CreateWalletInput targetInput = new CreateWalletInput(targetEmail, new BigDecimal("1"));
        String targetResponse = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(targetInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput targetOutput = objectMapper.readValue(targetResponse, CreateWalletOutput.class);

        // Perform transfer
        TransferInput transferInput = new TransferInput(
                sourceOutput.id(),
                targetOutput.id(),
                new BigDecimal("50")
        );

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.newBalance").value(50));
    }

    @Test
    void getTransactionsByWalletId_ShouldReturnTransactions() throws Exception {
        // Create source wallet
        String sourceEmail = generateUniqueEmail();
        CreateWalletInput sourceInput = new CreateWalletInput(sourceEmail, new BigDecimal("100"));
        String sourceResponse = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sourceInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput sourceOutput = objectMapper.readValue(sourceResponse, CreateWalletOutput.class);

        // Create target wallet
        String targetEmail = generateUniqueEmail();
        CreateWalletInput targetInput = new CreateWalletInput(targetEmail, new BigDecimal("1"));
        String targetResponse = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(targetInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput targetOutput = objectMapper.readValue(targetResponse, CreateWalletOutput.class);

        // Perform transfer
        TransferInput transferInput = new TransferInput(
                sourceOutput.id(),
                targetOutput.id(),
                new BigDecimal("50")
        );
        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferInput)));

        // Get transactions for source wallet
        mockMvc.perform(get("/transactions/{walletId}", sourceOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromId").value(sourceOutput.id().toString()))
                .andExpect(jsonPath("$[0].toId").value(targetOutput.id().toString()))
                .andExpect(jsonPath("$[0].amount").value(-50));

        // Get transactions for target wallet
        mockMvc.perform(get("/transactions/{walletId}", targetOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromId").value(sourceOutput.id().toString()))
                .andExpect(jsonPath("$[0].toId").value(targetOutput.id().toString()))
                .andExpect(jsonPath("$[0].amount").value(50));
    }

    @Test
    void getTransactionsByWalletId_WithPagination_ShouldReturnPaginatedTransactions() throws Exception {
        // Create source wallet
        String sourceEmail = generateUniqueEmail();
        CreateWalletInput sourceInput = new CreateWalletInput(sourceEmail, new BigDecimal("100"));
        String sourceResponse = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sourceInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput sourceOutput = objectMapper.readValue(sourceResponse, CreateWalletOutput.class);

        // Create target wallet
        String targetEmail = generateUniqueEmail();
        CreateWalletInput targetInput = new CreateWalletInput(targetEmail, new BigDecimal("1"));
        String targetResponse = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(targetInput)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        CreateWalletOutput targetOutput = objectMapper.readValue(targetResponse, CreateWalletOutput.class);

        // Perform multiple transfers
        for (int i = 0; i < 5; i++) {
            TransferInput transferInput = new TransferInput(
                    sourceOutput.id(),
                    targetOutput.id(),
                    new BigDecimal("10")
            );
            mockMvc.perform(post("/transactions/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transferInput)));
        }

        // Get first page of transactions
        mockMvc.perform(get("/transactions/{walletId}?limit=2&offset=0", sourceOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Get second page of transactions
        mockMvc.perform(get("/transactions/{walletId}?limit=2&offset=2", sourceOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Get third page of transactions
        mockMvc.perform(get("/transactions/{walletId}?limit=2&offset=4", sourceOutput.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
} 