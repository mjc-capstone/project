package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import java.util.Map;

record SeoulMasterSnapshot(
    Map<String, RegionEntity> regions,
    Map<String, BusinessCategoryEntity> categories,
    Map<String, MetricPeriodEntity> periods
) {
    RegionEntity region(String code) {
        return required(regions, code, "행정동");
    }

    BusinessCategoryEntity category(String code) {
        return required(categories, code, "업종");
    }

    MetricPeriodEntity period(String code) {
        return required(periods, code, "기간");
    }

    private static <T> T required(Map<String, T> values, String code, String label) {
        T value = values.get(code);
        if (value == null) {
            throw new IllegalStateException(label + " 마스터 동기화에 실패했습니다: " + code);
        }
        return value;
    }
}
