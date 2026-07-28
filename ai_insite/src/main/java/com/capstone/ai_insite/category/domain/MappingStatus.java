package com.capstone.ai_insite.category.domain;

public enum MappingStatus {
    UNRESOLVED,
    CANDIDATE,
    AUTO_CONFIRMED,
    CONFIRMED,
    REJECTED;

    public boolean isConfirmed() {
        return this == AUTO_CONFIRMED || this == CONFIRMED;
    }
}
