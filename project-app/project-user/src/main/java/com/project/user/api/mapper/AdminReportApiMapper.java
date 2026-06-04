package com.project.user.api.mapper;

import com.project.user.api.dto.ActiveTransferUserResponse;
import com.project.user.api.dto.OrphanWalletResponse;
import com.project.user.api.dto.UserWalletSummaryResponse;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminReportApiMapper {

    // 1. TEKIL MAPLEME METOTLARI (Listelerin icindeki objeleri cevirmek icin sarttir)
    UserWalletSummaryResponse toSummaryResponse(UserWalletSummaryModel model);
    ActiveTransferUserResponse toActiveUserResponse(ActiveTransferUserModel model);
    OrphanWalletResponse toOrphanWalletResponse(OrphanWalletModel model);

    // 2. LISTE MAPLEME METOTLARI
    List<UserWalletSummaryResponse> toSummaryResponseList(List<UserWalletSummaryModel> models);

    List<ActiveTransferUserResponse> toActiveUserResponseList(List<ActiveTransferUserModel> models);

    List<OrphanWalletResponse> toOrphanWalletResponseList(List<OrphanWalletModel> models);
}