package com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * DTO que representa el contrato JSON enviado en el cuerpo de la petición POST a {@code /accounts}.
 */
public record AccountCreationRestRequest(
    @JsonProperty("user_id") String userId,
    @JsonProperty("account_type") String accountType,
    @JsonProperty("balance") BigDecimal balance,
    @JsonProperty("currency") String currency,
    @JsonProperty("account_number") String accountNumber,
    @JsonProperty("user_email") String userEmail
) {}
