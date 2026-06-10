package com.project.transaction.infrastructure.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransferNotificationRequestTest {

    @Test
    void receivedTransfer_buildsStructuredPayload() {
        TransferNotificationRequest request =
                TransferNotificationRequest.receivedTransfer("tx-1", "receiver-1", 25.0);

        assertThat(request.eventId()).isEqualTo("tx-1");
        assertThat(request.type()).isEqualTo("TRANSFER_RECEIVED");
        assertThat(request.sourceService()).isEqualTo("enterprise-app");
        assertThat(request.recipientId()).isEqualTo("receiver-1");
        assertThat(request.title()).isEqualTo("Transfer received");
        assertThat(request.message()).contains("25.0 TRY");
        assertThat(request.referenceId()).isEqualTo("tx-1");
        assertThat(request.amount()).isEqualByComparingTo("25.0");
        assertThat(request.currency()).isEqualTo("TRY");
    }
}
