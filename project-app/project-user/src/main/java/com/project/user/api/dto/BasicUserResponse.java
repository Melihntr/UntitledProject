package com.project.user.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicUserResponse {
    private String id;
    private String username;
    // Email, createdAt, isActive gibi gizli/büyük alanları dışarı vermiyoruz!
}