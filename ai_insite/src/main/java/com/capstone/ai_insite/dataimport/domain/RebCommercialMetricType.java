package com.capstone.ai_insite.dataimport.domain;

public enum RebCommercialMetricType {
    RENT_AMOUNT,
    RENT_INDEX,
    VACANCY_RATE,
    INVESTMENT_RETURN_RATE;

    public boolean accepts(String itemName) {
        if (itemName == null) {
            return false;
        }
        String normalized = itemName.replace(" ", "");
        return switch (this) {
            case RENT_AMOUNT -> normalized.equals("임대료");
            case RENT_INDEX -> normalized.contains("임대가격지수");
            case VACANCY_RATE -> normalized.equals("공실률");
            case INVESTMENT_RETURN_RATE -> normalized.equals("투자수익률");
        };
    }
}
