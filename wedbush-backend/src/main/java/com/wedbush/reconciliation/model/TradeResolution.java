package com.wedbush.reconciliation.model;

public record TradeResolution(
    String transactionId,
    String security,
    String emailBody
) {}