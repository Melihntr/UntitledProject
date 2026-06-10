package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.port.ReportPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetActiveTransferUsersHandler implements UseCaseHandler<List<ActiveTransferUserModel>, Void> {

    private final ReportPort reportPort;

    @Override
    public List<ActiveTransferUserModel> handle(Void input) {
        return reportPort.getActiveTransferUsers();
    }
}
