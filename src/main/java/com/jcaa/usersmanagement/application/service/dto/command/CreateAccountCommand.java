package com.jcaa.usersmanagement.application.service.dto.command;

import com.jcaa.usersmanagement.domain.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Comando para solicitar la creación de una cuenta en el microservicio externo.
 *
 * <p>Aplica los valores por defecto especificados en el contrato:
 * <ul>
 *   <li>{@code balance}: por defecto {@code 0.00} si es nulo</li>
 *   <li>{@code currency}: por defecto {@code "COP"} si es nulo o vacío</li>
 * </ul>
 */
public record CreateAccountCommand(
    @NotBlank(message = "El user_id es obligatorio")
    String userId,

    @NotNull(message = "El account_type es obligatorio (SAVINGS o CHECKING)")
    AccountType accountType,

    @DecimalMin(value = "0.0", message = "El balance no puede ser negativo")
    BigDecimal balance,

    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres ISO 4217")
    String currency,

    @Size(max = 20, message = "El account_number no puede superar los 20 caracteres")
    String accountNumber,

    String userEmail
) {

  public CreateAccountCommand {
    if (balance == null) {
      balance = BigDecimal.ZERO;
    }
    if (currency == null || currency.isBlank()) {
      currency = "COP";
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String userId;
    private AccountType accountType;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "COP";
    private String accountNumber;
    private String userEmail;

    public Builder userId(final String userId) {
      this.userId = userId;
      return this;
    }

    public Builder accountType(final AccountType accountType) {
      this.accountType = accountType;
      return this;
    }

    public Builder balance(final BigDecimal balance) {
      this.balance = balance;
      return this;
    }

    public Builder currency(final String currency) {
      this.currency = currency;
      return this;
    }

    public Builder accountNumber(final String accountNumber) {
      this.accountNumber = accountNumber;
      return this;
    }

    public Builder userEmail(final String userEmail) {
      this.userEmail = userEmail;
      return this;
    }

    public CreateAccountCommand build() {
      return new CreateAccountCommand(userId, accountType, balance, currency, accountNumber, userEmail);
    }
  }
}
