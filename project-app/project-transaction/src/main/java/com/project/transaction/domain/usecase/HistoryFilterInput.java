package com.project.transaction.domain.usecase;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Getter
@Builder
public class HistoryFilterInput {

    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Pageable pageable;
}
