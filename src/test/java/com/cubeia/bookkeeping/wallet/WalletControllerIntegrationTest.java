package com.cubeia.bookkeeping.wallet;

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
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class WalletControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private String generateUniqueEmail() {
    return "test-" + UUID.randomUUID() + "@example.com";
  }

  @Test
  void createWallet_ShouldReturnCreatedWallet() throws Exception {
    String uniqueEmail = generateUniqueEmail();
    CreateWalletInput input = new CreateWalletInput(uniqueEmail, new BigDecimal("100"));

    mockMvc.perform(post("/wallets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(input)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void getWallet_ShouldReturnWallet() throws Exception {
    String uniqueEmail = generateUniqueEmail();
    CreateWalletInput input = new CreateWalletInput(uniqueEmail, new BigDecimal("100"));
    String response = mockMvc.perform(post("/wallets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(input)))
      .andReturn()
      .getResponse()
      .getContentAsString();

    CreateWalletOutput output = objectMapper.readValue(response, CreateWalletOutput.class);

    mockMvc.perform(get("/wallets/{id}/balance", output.id()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(output.id().toString()))
      .andExpect(jsonPath("$.balance").value(100));
  }

} 