package com.capstone.ai_insite.metric.domain.policy;

import java.util.Set;

public class BuildingUsePolicy {

    private static final Set<String> COMMERCIAL_USE_CODES = Set.of(
        "03000", "04000", "05000", "07000", "08000", "09000",
        "10000", "13000", "14000", "15000", "16000", "20000",
        "24000", "27000"
    );

    public boolean isCommercial(String mainUseCode) {
        return mainUseCode != null
            && COMMERCIAL_USE_CODES.contains(mainUseCode.trim());
    }
}
