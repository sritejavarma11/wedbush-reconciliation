package com.wedbush.reconciliation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String transactionId;

    @NotNull
    @Column(nullable = false)
    private String tickerSymbol;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal commission;

    @NotNull
    @Column(nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Source source;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private MatchStatus matchStatus;

    @Column(length = 1000)
    private String discrepancyNotes;
}
