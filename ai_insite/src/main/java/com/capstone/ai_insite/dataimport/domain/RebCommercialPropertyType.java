package com.capstone.ai_insite.dataimport.domain;

public enum RebCommercialPropertyType {
    SMALL_RETAIL(
        "T248223134698125",
        "T241273134677393",
        "T241833134686576",
        "T246253134913401"
    ),
    MEDIUM_LARGE_RETAIL(
        "T244363134858603",
        "T249863134832916",
        "T249633134845544",
        "T242083134887473"
    ),
    COLLECTIVE_RETAIL(
        "T244913134948657",
        "T242433134965708",
        "T243283134931290",
        "T246393134978815"
    ),
    OFFICE(
        "TT249843134237374",
        "TT244233134228593",
        "TT244763134428698",
        "T245883135037859"
    );

    private final String rentTableId;
    private final String rentIndexTableId;
    private final String vacancyTableId;
    private final String returnTableId;

    RebCommercialPropertyType(
        String rentTableId,
        String rentIndexTableId,
        String vacancyTableId,
        String returnTableId
    ) {
        this.rentTableId = rentTableId;
        this.rentIndexTableId = rentIndexTableId;
        this.vacancyTableId = vacancyTableId;
        this.returnTableId = returnTableId;
    }

    public String tableId(RebCommercialMetricType metricType) {
        return switch (metricType) {
            case RENT_AMOUNT -> rentTableId;
            case RENT_INDEX -> rentIndexTableId;
            case VACANCY_RATE -> vacancyTableId;
            case INVESTMENT_RETURN_RATE -> returnTableId;
        };
    }
}
