package com.project.user.infrastructure.api.mapper;

import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminReportApiMapperTest {

    private final AdminReportApiMapper mapper = new AdminReportApiMapperImpl();

    @Test
    void mapsModelsAndLists() {
        UserWalletSummaryModel summary = UserWalletSummaryModel.builder()
                .username("alice").email("alice@example.com").balance(BigDecimal.TEN).build();
        ActiveTransferUserModel active = ActiveTransferUserModel.builder()
                .username("alice").amount(BigDecimal.ONE).createdAt(LocalDateTime.now()).build();
        OrphanWalletModel orphan = OrphanWalletModel.builder()
                .walletId("w1").balance(BigDecimal.ZERO).supposedUserId("missing").build();

        assertThat(mapper.toSummaryResponse(summary).getEmail()).isEqualTo("alice@example.com");
        assertThat(mapper.toActiveUserResponse(active).getAmount()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(mapper.toOrphanWalletResponse(orphan).getWalletId()).isEqualTo("w1");
        assertThat(mapper.toSummaryResponseList(List.of(summary))).hasSize(1);
        assertThat(mapper.toActiveUserResponseList(List.of(active))).hasSize(1);
        assertThat(mapper.toOrphanWalletResponseList(List.of(orphan))).hasSize(1);
    }

    @Test
    void nullInputsReturnNull() {
        assertThat(mapper.toSummaryResponse(null)).isNull();
        assertThat(mapper.toActiveUserResponse(null)).isNull();
        assertThat(mapper.toOrphanWalletResponse(null)).isNull();
        assertThat(mapper.toSummaryResponseList(null)).isNull();
        assertThat(mapper.toActiveUserResponseList(null)).isNull();
        assertThat(mapper.toOrphanWalletResponseList(null)).isNull();
    }
}
