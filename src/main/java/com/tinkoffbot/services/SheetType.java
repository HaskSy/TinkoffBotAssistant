package com.tinkoffbot.services;

import lombok.Getter;

@Getter
public enum SheetType {

    STATS("stats"),
    REPORT_STATS("report_stats");

    private final String type;

    SheetType(String type) {
        this.type = type;
    }
}
