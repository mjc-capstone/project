package com.capstone.ai_insite.region.domain;

public enum RegionMappingStatus {
    CANDIDATE,
    AUTO_CONFIRMED,
    CONFIRMED,
    REJECTED;

    public boolean isUsable() {
        return this == AUTO_CONFIRMED || this == CONFIRMED;
    }
}
