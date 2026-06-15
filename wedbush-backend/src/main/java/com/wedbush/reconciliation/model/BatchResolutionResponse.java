package com.wedbush.reconciliation.model;

import java.util.List;

public record BatchResolutionResponse(
    List<TradeResolution> resolutions
) {}