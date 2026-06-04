package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.port.ReportPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetActiveTransferUsersHandler implements UseCaseHandler<List<ActiveTransferUserModel>, Void> {

    private final ReportPort reportPort;

    public GetActiveTransferUsersHandler(ReportPort reportPort) {
        this.reportPort = reportPort;
    }

    @Override
    public List<ActiveTransferUserModel> handle(Void input) {
        return reportPort.getActiveTransferUsers();
    }
}