package com.project.user.infrastructure.api.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor  
@AllArgsConstructor
public class ActiveTransferUserResponse {
    private String username;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}