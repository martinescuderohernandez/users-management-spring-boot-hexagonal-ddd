package com.jcaa.usersmanagement.infrastructure.adapter.client.account.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.domain.enums.AccountType;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto.AccountCreationRestRequest;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto.AccountRestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountClientMapper")
class AccountClientMapperTest {

  @Test
  @DisplayName("Debe mapear CreateAccountCommand a AccountCreationRestRequest correctamente")
  void shouldMapCommandToRestRequest() {
    final CreateAccountCommand command = CreateAccountCommand.builder()
        .userId("usr-1")
        .accountType(AccountType.SAVINGS)
        .balance(new BigDecimal("100.00"))
        .currency("COP")
        .accountNumber("123456")
        .userEmail("usr1@test.com")
        .build();

    final AccountCreationRestRequest request = AccountClientMapper.toRestRequest(command);

    assertNotNull(request);
    assertEquals("usr-1", request.userId());
    assertEquals("SAVINGS", request.accountType());
    assertEquals(new BigDecimal("100.00"), request.balance());
    assertEquals("COP", request.currency());
    assertEquals("123456", request.accountNumber());
    assertEquals("usr1@test.com", request.userEmail());
  }

  @Test
  @DisplayName("Debe retornar null al mapear comando nulo")
  void shouldReturnNullWhenCommandIsNull() {
    assertNull(AccountClientMapper.toRestRequest(null));
  }

  @Test
  @DisplayName("Debe mapear AccountRestResponse a AccountResponse correctamente")
  void shouldMapRestResponseToAccountResponse() {
    final LocalDateTime now = LocalDateTime.now();
    final AccountRestResponse restResponse = new AccountRestResponse(
        "acc-1",
        "123456",
        "usr-1",
        "SAVINGS",
        BigDecimal.TEN,
        "COP",
        "ACTIVE",
        now,
        now
    );

    final AccountResponse response = AccountClientMapper.toResponse(restResponse);

    assertNotNull(response);
    assertEquals("acc-1", response.id());
    assertEquals("123456", response.accountNumber());
    assertEquals("usr-1", response.userId());
    assertEquals("SAVINGS", response.accountType());
    assertEquals(BigDecimal.TEN, response.balance());
    assertEquals("COP", response.currency());
    assertEquals("ACTIVE", response.status());
    assertEquals(now, response.createdAt());
    assertEquals(now, response.updatedAt());
  }

  @Test
  @DisplayName("Debe retornar null al mapear respuesta nula")
  void shouldReturnNullWhenRestResponseIsNull() {
    assertNull(AccountClientMapper.toResponse(null));
  }
}
