package com.project.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Yonetici raporlari icin kullanici ve cuzdan bilgilerini birlestiren Domain modeli.
 * Entity'den tamamen bagimsizdir ve sadece is mantiginin ihtiyac duydugu verileri tasir.
 */
@Getter
@Builder
public class UserWalletSummaryModel {

    private String username;
    private String email;
    private BigDecimal balance;

    // Raporlarda null bakiyeleri (henuz cuzdani olmayanlar icin) 0 olarak gostermek icin yardimci metot
    public BigDecimal getSafeBalance() {
        return balance != null ? balance : BigDecimal.ZERO;
    }
}