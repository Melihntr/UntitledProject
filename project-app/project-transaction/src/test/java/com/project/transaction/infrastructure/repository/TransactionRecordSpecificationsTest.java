package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionRecordSpecificationsTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void historyForUser_buildsSenderReceiverAndDateRangeCriteria() {
        String userId = "user-1";
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);
        Root<TransactionRecordEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path sender = mock(Path.class);
        Path receiver = mock(Path.class);
        Path senderId = mock(Path.class);
        Path receiverId = mock(Path.class);
        Path transactionDate = mock(Path.class);
        Predicate senderPredicate = mock(Predicate.class);
        Predicate receiverPredicate = mock(Predicate.class);
        Predicate participantPredicate = mock(Predicate.class);
        Predicate datePredicate = mock(Predicate.class);
        Predicate expected = mock(Predicate.class);
        when(root.get("sender")).thenReturn(sender);
        when(root.get("receiver")).thenReturn(receiver);
        when(root.get("transactionDate")).thenReturn(transactionDate);
        when(sender.get("id")).thenReturn(senderId);
        when(receiver.get("id")).thenReturn(receiverId);
        when(builder.equal(senderId, userId)).thenReturn(senderPredicate);
        when(builder.equal(receiverId, userId)).thenReturn(receiverPredicate);
        when(builder.or(senderPredicate, receiverPredicate)).thenReturn(participantPredicate);
        when(builder.between(transactionDate, start, end)).thenReturn(datePredicate);
        when(builder.and(participantPredicate, datePredicate)).thenReturn(expected);
        Specification<TransactionRecordEntity> specification =
                TransactionRecordSpecifications.historyForUser(userId, start, end);

        Predicate result = specification.toPredicate(root, query, builder);

        assertThat(result).isSameAs(expected);
        verify(builder).or(senderPredicate, receiverPredicate);
        verify(builder).between(transactionDate, start, end);
        verify(builder).and(participantPredicate, datePredicate);
    }
}
