package com.project.user.domain.handler;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.port.ReportPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetActiveTransferUsersHandler implements UseCaseHandler<List<ActiveTransferUserModel>, Void> {

    private final ReportPort reportPort;

    @Override
    public List<ActiveTransferUserModel> handle(Void input) {
        log.info("report.active-transfer-users.request");
        List<ActiveTransferUserModel> result = reportPort.getActiveTransferUsers();
        log.info("report.active-transfer-users.success resultCount={}", result.size());
        return result;
    }
}
