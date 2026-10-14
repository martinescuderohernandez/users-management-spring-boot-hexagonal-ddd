package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.CreateAccountUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.infrastructure.security.JwtAuthenticationFilter;
import com.jcaa.usersmanagement.infrastructure.security.JwtTokenService;
import com.jcaa.usersmanagement.infrastructure.security.RestAccessDeniedHandler;
import com.jcaa.usersmanagement.infrastructure.security.RestAuthenticationEntryPoint;
import com.jcaa.usersmanagement.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@WebMvcTest(
    controllers = AccountRestController.class,
    properties = "spring.main.web-application-type=servlet")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("AccountRestController")
class AccountRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateAccountUseCase createAccountUseCase;
  @MockBean private JwtTokenService jwtTokenService;

  @Test
  @DisplayName("Debe crear una cuenta retornando HTTP 201 Created")
  void shouldCreateAccountSuccessfully() throws Exception {
    final AccountResponse expectedResponse =
        new AccountResponse(
            "acc-123",
            "1234567890",
            "usr-001",
            "SAVINGS",
            new BigDecimal("50000.00"),
            "COP",
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(createAccountUseCase.execute(any(CreateAccountCommand.class))).thenReturn(expectedResponse);

    final String requestBody = """
        {
          "user_id": "usr-001",
          "account_type": "SAVINGS",
          "balance": 50000.00,
          "currency": "COP",
          "account_number": "1234567890",
          "user_email": "usr@example.com"
        }
        """;

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("acc-123"))
        .andExpect(jsonPath("$.account_number").value("1234567890"))
        .andExpect(jsonPath("$.user_id").value("usr-001"))
        .andExpect(jsonPath("$.account_type").value("SAVINGS"))
        .andExpect(jsonPath("$.currency").value("COP"));

    verify(createAccountUseCase).execute(any(CreateAccountCommand.class));
  }

  @Test
  @DisplayName("Debe fallar con HTTP 400 Bad Request si faltan campos obligatorios")
  void shouldReturn400WhenMissingRequiredFields() throws Exception {
    final String invalidBody = """
        {
          "balance": -50.00
        }
        """;

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
        .andExpect(status().isBadRequest());
  }
}
