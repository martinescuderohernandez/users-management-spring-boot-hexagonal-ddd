package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountRestResponse(
    String id,
    @JsonProperty("account_number") String accountNumber,
    @JsonProperty("user_id") String userId,
    @JsonProperty("account_type") String accountType,
    BigDecimal balance,
    String currency,
    String status,
    @JsonProperty("created_at") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
    @JsonProperty("updated_at") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt
) {}
