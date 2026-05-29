package com.project.common.event;

// Sadece kullanıcının ID'sini taşıyan hafif bir Record nesnesi
public record UserCreatedEvent(String userId) {}