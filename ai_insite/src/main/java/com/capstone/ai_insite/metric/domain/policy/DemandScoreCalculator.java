package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.DemandMetric;
import java.math.BigDecimal;
import java.util.Arrays;

public class DemandScoreCalculator {

    public BigDecimal calculate(DemandMetric demand) {
        return ScoreMath.average(Arrays.asList(
            demand.residentialDemandScore(),
            demand.officeDemandScore(),
            demand.attractionScore(),
            demand.trafficAccessScore()
        ));
    }
}
