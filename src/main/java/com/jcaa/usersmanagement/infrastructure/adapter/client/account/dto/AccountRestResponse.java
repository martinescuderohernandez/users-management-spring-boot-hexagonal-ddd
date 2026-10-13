package com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa el contrato JSON recibido con HTTP 201 Created desde {@code /accounts}.
 */
public record AccountRestResponse(
    @JsonProperty("id") String id,
    @JsonProperty("account_number") String accountNumber,
    @JsonProperty("user_id") String userId,
    @JsonProperty("account_type") String accountType,
    @JsonProperty("balance") BigDecimal balance,
    @JsonProperty("currency") String currency,
    @JsonProperty("status") String status,
    @JsonProperty("created_at") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
    @JsonProperty("updated_at") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt
) {}
