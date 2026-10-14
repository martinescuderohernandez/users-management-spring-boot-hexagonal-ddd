package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jcaa.usersmanagement.domain.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRestRequest(
    @NotBlank(message = "user_id must not be blank")
    @JsonProperty("user_id")
    String userId,

    @NotNull(message = "account_type is mandatory (SAVINGS or CHECKING)")
    @JsonProperty("account_type")
    AccountType accountType,

    @DecimalMin(value = "0.0", message = "balance cannot be negative")
    @JsonProperty("balance")
    BigDecimal balance,

    @Size(min = 3, max = 3, message = "currency must be 3 characters ISO 4217")
    @JsonProperty("currency")
    String currency,

    @Size(max = 20, message = "account_number cannot exceed 20 characters")
    @JsonProperty("account_number")
    String accountNumber,

    @JsonProperty("user_email")
    String userEmail
) {}
