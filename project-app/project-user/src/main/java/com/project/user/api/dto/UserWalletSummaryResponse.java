package com.project.user.api.dto;

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
public class UserWalletSummaryResponse {
    private String username;
    private String email;
    private BigDecimal balance;
}
