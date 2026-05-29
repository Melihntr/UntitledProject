package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;

import java.util.List;

// Input almadığı için Input tipine 'Void' veriyoruz
public class GetBasicUsersHandler implements UseCaseHandler<List<UserModel>, Void> {

    private final UserPort userPort;

    public GetBasicUsersHandler(UserPort userPort) {
        this.userPort = userPort;
    }

    @Override
    public List<UserModel> handle(Void input) {
        return userPort.getAllUsers();
    }
}