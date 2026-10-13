package com.jcaa.usersmanagement.application.service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que encapsula la información de la cuenta creada en el microservicio externo.
 */
public record AccountResponse(
    String id,
    String accountNumber,
    String userId,
    String accountType,
    BigDecimal balance,
    String currency,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
