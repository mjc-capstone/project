from __future__ import annotations

from hashlib import sha256
import json


FEATURE_SCHEMA_VERSION = "core-feature-v1"

CATEGORICAL_FEATURES = (
    "regionCodeIndex",
    "categoryCodeIndex",
)

NUMERIC_FEATURES = (
    "salesAmountLog1p",
    "salesCountLog1p",
    "salesGrowthRateQoq",
    "storeCount",
    "storeGrowthRateQoq",
    "demandScore",
    "competitionScore",
    "marketScore",
    "stabilityScore",
    "closureRiskSignal",
    "quarterOfYear",
    "periodOrdinal",
)

FEATURE_NAMES = CATEGORICAL_FEATURES + NUMERIC_FEATURES

RAW_NUMERIC_FEATURES = (
    "salesAmount",
    "salesCount",
    "salesGrowthRateQoq",
    "storeCount",
    "storeGrowthRateQoq",
    "demandScore",
    "competitionScore",
    "marketScore",
    "stabilityScore",
    "closureRiskSignal",
)

TARGETS = {
    "nextQuarterSalesGrowthRate": "regression",
    "nextQuarterStoreCountDeclined": "binary",
    "nextQuarterCloseRate": "regression",
    "fourQuarterStoreRetentionRate": "regression",
    "fourQuarterStoreBaseMaintained": "binary",
}


def feature_schema_hash() -> str:
    payload = json.dumps(
        {
            "version": FEATURE_SCHEMA_VERSION,
            "categorical": CATEGORICAL_FEATURES,
            "numeric": NUMERIC_FEATURES,
        },
        ensure_ascii=True,
        separators=(",", ":"),
    )
    return sha256(payload.encode("ascii")).hexdigest()
