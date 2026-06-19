package com.project.user.infrastructure.api.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor  
@AllArgsConstructor
public class OrphanWalletResponse {
    private String walletId;
    private BigDecimal balance;
    private String supposedUserId;
}
