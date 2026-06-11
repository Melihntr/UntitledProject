package com.project.transaction.domain.usecase;

import com.project.transaction.domain.port.TransactionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case handler responsible for identifying and retrieving suspicious or potentially fraudulent transfers.
 * This administrative domain logic delegates to the outbound port, which executes the necessary 
 * complex aggregations or anomaly detection queries in the infrastructure layer.
 * * Enterprise Note: While @Service is used here for convenient Spring IoC container registration, 
 * strictly pure Clean Architecture often prefers registering domain services via a separate 
 * @Configuration class in the infrastructure layer to keep the core domain 100% framework-agnostic.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckSuspiciousTransfersHandler {

    private final TransactionPort transactionPort;

    /**
     * Executes the business logic to fetch suspicious transaction records.
     * Because this action requires no external input parameters, it directly invokes the port.
     * * Future Extension Point: This method is the ideal location to orchestrate additional 
     * domain logic, such as publishing a "FraudDetectedEvent" to Kafka or triggering 
     * an alert to the risk management team.
     *
     * @return A list of raw object arrays representing aggregated suspicious transaction data.
     */
    public List<Object[]> handle() {
        log.info("fraud-report.request");
        List<Object[]> result = transactionPort.getSuspiciousTransfers();
        log.info("fraud-report.success resultCount={}", result.size());
        return result;
    }
}
