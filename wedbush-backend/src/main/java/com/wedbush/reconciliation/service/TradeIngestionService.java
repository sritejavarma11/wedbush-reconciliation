package com.wedbush.reconciliation.service;

import com.opencsv.CSVReader;
import com.wedbush.reconciliation.model.MatchStatus;
import com.wedbush.reconciliation.model.Source;
import com.wedbush.reconciliation.model.Trade;
import com.wedbush.reconciliation.repository.TradeRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradeIngestionService {

    private static final int TRANSACTION_ID = 0;
    private static final int TICKER_SYMBOL = 1;
    private static final int QUANTITY = 2;
    private static final int PRICE = 3;
    private static final int COMMISSION = 4;
    private static final int TRANSACTION_DATE = 5;

    private final TradeRepository tradeRepository;

    public TradeIngestionService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void ingestInternalTrades(String filePath) {
        List<Trade> trades = new ArrayList<>();

        try (Reader reader = new InputStreamReader(
                new ClassPathResource(filePath).getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext();

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (isBlankRow(row)) {
                    continue;
                }
                trades.add(mapRowToTrade(row));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to ingest internal trades from " + filePath, e);
        }

        List<Trade> savedTrades = tradeRepository.saveAll(trades);
        System.out.println("Successfully ingested " + savedTrades.size() + " internal trade records.");
    }

    private Trade mapRowToTrade(String[] row) {
        Trade trade = new Trade();
        trade.setTransactionId(requireField(row, TRANSACTION_ID, "transactionId"));
        trade.setTickerSymbol(requireField(row, TICKER_SYMBOL, "tickerSymbol"));
        trade.setQuantity(parseBigDecimal(row, QUANTITY, "quantity"));
        trade.setPrice(parseBigDecimal(row, PRICE, "price"));
        trade.setCommission(parseBigDecimal(row, COMMISSION, "commission"));
        trade.setTransactionDate(parseDate(row, TRANSACTION_DATE));
        trade.setSource(Source.INTERNAL);
        trade.setMatchStatus(MatchStatus.PENDING);
        return trade;
    }

    private String requireField(String[] row, int index, String fieldName) {
        if (index >= row.length || row[index] == null || row[index].isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return row[index].trim();
    }

    private BigDecimal parseBigDecimal(String[] row, int index, String fieldName) {
        String value = requireField(row, index, fieldName);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value for " + fieldName + ": " + value, e);
        }
    }

    private LocalDate parseDate(String[] row, int index) {
        String value = requireField(row, index, "transactionDate");
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date for transactionDate: " + value, e);
        }
    }

    private boolean isBlankRow(String[] row) {
        if (row.length == 0) {
            return true;
        }
        for (String value : row) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
