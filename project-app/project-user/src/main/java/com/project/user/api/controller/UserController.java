package com.project.user.api.controller;

import com.project.common.model.GenericResponse;
import com.project.user.api.dto.BasicUserResponse;
import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.api.mapper.UserApiMapper;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler; // Import eklendi
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationEventPublisher;
import com.project.common.event.UserCreatedEvent;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserHandler createUserHandler;
    private final GetBasicUsersHandler getBasicUsersHandler;
    private final UserApiMapper userApiMapper;
    private final ApplicationEventPublisher eventPublisher; // 1. Radarımız eklendi

    public UserController(CreateUserHandler createUserHandler, 
                          GetBasicUsersHandler getBasicUsersHandler, 
                          UserApiMapper userApiMapper,
                          ApplicationEventPublisher eventPublisher) { // 2. Constructor güncellendi
        this.createUserHandler = createUserHandler;
        this.getBasicUsersHandler = getBasicUsersHandler;
        this.userApiMapper = userApiMapper;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<CreateUserResponse>> createUser(@RequestBody CreateUserRequest request) {
        UserCreateInput input = userApiMapper.toInput(request);
        UserModel createdUser = createUserHandler.handle(input);
        
        // 3. İŞTE BURASI! Kullanıcı veritabanına yazıldığı an sisteme bağırıyoruz:
        eventPublisher.publishEvent(new UserCreatedEvent(createdUser.getId()));

        CreateUserResponse responseDto = userApiMapper.toResponse(createdUser);
        return ResponseEntity.ok(GenericResponse.success(responseDto));
    }

    @GetMapping("/basic-list")
    public ResponseEntity<GenericResponse<List<BasicUserResponse>>> getBasicUsers() {
        // Parametre almadığı için null gönderiyoruz
        List<UserModel> users = getBasicUsersHandler.handle(null);
        
        List<BasicUserResponse> responseList = users.stream()
                .map(user -> BasicUserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
                
        return ResponseEntity.ok(GenericResponse.success(responseList));
    }
}