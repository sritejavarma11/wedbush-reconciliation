package com.wedbush.reconciliation.repository;

import com.wedbush.reconciliation.model.MatchStatus;
import com.wedbush.reconciliation.model.Source;
import com.wedbush.reconciliation.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    Optional<Trade> findByTransactionId(String transactionId);

    List<Trade> findByMatchStatus(MatchStatus status);

    List<Trade> findBySourceAndMatchStatus(Source source, MatchStatus status);
}
