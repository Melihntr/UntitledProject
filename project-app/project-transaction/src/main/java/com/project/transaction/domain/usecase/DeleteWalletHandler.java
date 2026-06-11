package com.project.transaction.domain.usecase;

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
    public void handle(String userId) {
        log.info("Deleting wallet record for user ID: {}", userId);
        transactionPort.deleteWalletByUserId(userId);
        log.info("Successfully deleted wallet record for user ID: {}", userId);
    }
}
