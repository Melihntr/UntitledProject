package com.project.user.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ActiveTransferUserModel {
    private String username;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}