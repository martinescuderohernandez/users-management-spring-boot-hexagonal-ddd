package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.out.AccountClientPort;
import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.domain.enums.AccountType;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AccountService")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock
  private AccountClientPort accountClientPort;

  private AccountService accountService;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      accountService = new AccountService(accountClientPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("Debe delegar la creación de cuenta al puerto de salida exitosamente")
  void shouldDelegateAccountCreationToOutboundPortSuccessfully() {
    final CreateAccountCommand command = CreateAccountCommand.builder()
        .userId("usr-777")
        .accountType(AccountType.SAVINGS)
        .balance(new BigDecimal("250.00"))
        .currency("COP")
        .userEmail("test@example.com")
        .build();

    final AccountResponse expectedResponse = new AccountResponse(
        "acc-123",
        "9988776655",
        "usr-777",
        "SAVINGS",
        new BigDecimal("250.00"),
        "COP",
        "ACTIVE",
        LocalDateTime.now(),
        LocalDateTime.now()
    );

    when(accountClientPort.createAccount(command)).thenReturn(expectedResponse);

    final AccountResponse actualResponse = accountService.execute(command);

    assertNotNull(actualResponse);
    assertEquals("acc-123", actualResponse.id());
    assertEquals("9988776655", actualResponse.accountNumber());
    verify(accountClientPort, times(1)).createAccount(command);
  }

  @Test
  @DisplayName("Debe lanzar ConstraintViolationException si el user_id está en blanco")
  void shouldThrowConstraintViolationExceptionWhenUserIdIsBlank() {
    final CreateAccountCommand command = new CreateAccountCommand(
        "",
        AccountType.SAVINGS,
        BigDecimal.ZERO,
        "COP",
        null,
        "test@example.com"
    );

    assertThrows(ConstraintViolationException.class, () -> accountService.execute(command));
    verifyNoInteractions(accountClientPort);
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el comando es nulo")
  void shouldThrowIllegalArgumentExceptionWhenCommandIsNull() {
    assertThrows(IllegalArgumentException.class, () -> accountService.execute(null));
    verifyNoInteractions(accountClientPort);
  }
}
