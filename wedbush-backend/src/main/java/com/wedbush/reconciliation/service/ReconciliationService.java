package com.wedbush.reconciliation.service;

import com.wedbush.reconciliation.model.MatchStatus;
import com.wedbush.reconciliation.model.Source;
import com.wedbush.reconciliation.model.Trade;
import com.wedbush.reconciliation.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private final TradeRepository tradeRepository;

    public ReconciliationService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void reconcilePendingTrades() {
        List<Trade> internalTrades =
                tradeRepository.findBySourceAndMatchStatus(Source.INTERNAL, MatchStatus.PENDING);
        List<Trade> brokerTrades =
                tradeRepository.findBySourceAndMatchStatus(Source.BROKER, MatchStatus.PENDING);

        System.out.println("DEBUG - Internal PENDING trades loaded: " + internalTrades.size());
        for(Trade t : internalTrades) {
             System.out.println("   -> Internal ID: '" + t.getTransactionId() + "'");
        }

        System.out.println("DEBUG - Broker PENDING trades loaded: " + brokerTrades.size());
        for(Trade t : brokerTrades) {
             System.out.println("   -> Broker ID: '" + t.getTransactionId() + "'");
        }

        // ... rest of your existing logic with the Maps and .trim() ...

        // DEBUG: Verify the database is actually handing us the records
        System.out.println("DEBUG - Internal PENDING trades loaded: " + internalTrades.size());
        System.out.println("DEBUG - Broker PENDING trades loaded: " + brokerTrades.size());

        // FIX: Force .trim() on the transaction ID so hidden spaces don't break the Map keys
        Map<String, Trade> brokerTradeMap = brokerTrades.stream()
                .collect(Collectors.toMap(
                        trade -> trade.getTransactionId().trim(), 
                        trade -> trade, 
                        (existing, duplicate) -> existing
                ));

        int matchedCount = 0;
        int flaggedCount = 0;
        List<Trade> modifiedTrades = new ArrayList<>();

        for (Trade internalTrade : internalTrades) {
            // FIX: Force .trim() on the lookup key as well
            String lookupId = internalTrade.getTransactionId().trim();
            Trade brokerTrade = brokerTradeMap.get(lookupId);
            
            if (brokerTrade == null) {
                continue;
            }

            boolean quantityMatches = internalTrade.getQuantity().compareTo(brokerTrade.getQuantity()) == 0;
            boolean priceMatches = internalTrade.getPrice().compareTo(brokerTrade.getPrice()) == 0;

            if (quantityMatches && priceMatches) {
                internalTrade.setMatchStatus(MatchStatus.MATCHED);
                brokerTrade.setMatchStatus(MatchStatus.MATCHED);
                matchedCount++;
            } else {
                String discrepancyNotes = buildDiscrepancyNotes(
                        internalTrade, brokerTrade, quantityMatches, priceMatches);
                internalTrade.setMatchStatus(MatchStatus.FLAGGED);
                brokerTrade.setMatchStatus(MatchStatus.FLAGGED);
                internalTrade.setDiscrepancyNotes(discrepancyNotes);
                brokerTrade.setDiscrepancyNotes(discrepancyNotes);
                flaggedCount++;
            }

            modifiedTrades.add(internalTrade);
            modifiedTrades.add(brokerTrade);
        }

        tradeRepository.saveAll(modifiedTrades);
        System.out.println("Reconciliation complete: " + matchedCount + " matched, " + flaggedCount + " flagged");
    }

    private String buildDiscrepancyNotes(Trade internalTrade,
                                         Trade brokerTrade,
                                         boolean quantityMatches,
                                         boolean priceMatches) {
        List<String> notes = new ArrayList<>();

        if (!quantityMatches) {
            notes.add("Quantity mismatch: Internal [" + internalTrade.getQuantity()
                    + "] vs Broker [" + brokerTrade.getQuantity() + "]");
        }
        if (!priceMatches) {
            notes.add("Price mismatch: Internal [" + internalTrade.getPrice()
                    + "] vs Broker [" + brokerTrade.getPrice() + "]");
        }

        return String.join("; ", notes);
    }
}