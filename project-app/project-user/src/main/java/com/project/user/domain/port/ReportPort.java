package com.project.user.domain.port;

import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;

import java.util.List;

/**
 * Raporlama islevleri icin disari acilan (Outbound) Domain Port.
 * Hexagonal mimaride Domain katmani, veritabanina nasil baglanilacagini bilmez,
 * sadece bu arayuz uzerinden veriyi talep eder.
 */
public interface ReportPort {

    /**
     * Sistemdeki kullanicilarin cuzdan bakiyeleriyle birlikte ozetini dondurur. (LEFT JOIN)
     * @return Kullanici-Cuzdan ozet modelleri listesi.
     */
    List<UserWalletSummaryModel> getUserWalletSummaries();

    /**
     * Sadece basarili para transferi yapmis aktif kullanicilari dondurur. (INNER JOIN)
     * @return Aktif kullanici modelleri listesi.
     */
    List<ActiveTransferUserModel> getActiveTransferUsers();

    /**
     * Kullanicisi silinmis ancak cuzdani veritabaninda asili (orphan) kalmis hatalari bulur. (RIGHT JOIN / NATIVE)
     * @return Yetim cuzdan modelleri listesi.
     */
    List<OrphanWalletModel> getOrphanWallets();

}