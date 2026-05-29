package com.project.transaction.domain.usecase;

import com.project.transaction.domain.port.TransactionPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckSuspiciousTransfersHandler {

    private final TransactionPort transactionPort;

    public CheckSuspiciousTransfersHandler(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    // Herhangi bir parametre almadığı için direkt List dönüyoruz
    public List<Object[]> handle() {
        // Burada ekstra loglama veya bildirim atma gibi iş kuralları da işletilebilir
        return transactionPort.getSuspiciousTransfers();
    }
}