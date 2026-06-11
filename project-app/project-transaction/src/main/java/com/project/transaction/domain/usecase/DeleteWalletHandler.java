package com.project.transaction.domain.usecase;

import com.project.common.exception.AccessDeniedException;
import com.project.common.exception.ResourceNotFoundException;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteWalletHandler {

    private final TransactionPort transactionPort;

    @Transactional
    public void handle(DeleteWalletInput input) {
        log.info("wallet.delete.request walletId={} requestedByUserId={}", input.walletId(), input.requestedByUserId());

        WalletModel wallet = transactionPort.findWalletById(input.walletId())
                .orElseThrow(() -> {
                    log.warn("wallet.delete.not-found walletId={} requestedByUserId={}",
                            input.walletId(), input.requestedByUserId());
                    return new ResourceNotFoundException("Wallet not found with ID: " + input.walletId());
                });

        if (!wallet.getUserId().equals(input.requestedByUserId())) {
            log.warn("wallet.delete.denied walletId={} ownerUserId={} requestedByUserId={}",
                    input.walletId(), wallet.getUserId(), input.requestedByUserId());
            throw new AccessDeniedException("You can only delete your own wallet.");
        }

        transactionPort.deleteWalletById(input.walletId());
        log.info("wallet.delete.success walletId={} ownerUserId={}", input.walletId(), wallet.getUserId());
    }

    public record DeleteWalletInput(String walletId, String requestedByUserId) {
    }
}
