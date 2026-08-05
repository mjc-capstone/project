from __future__ import annotations

from dataclasses import dataclass
from hashlib import sha256
import json


CORE_PROFILE = "core"
ENRICHED_PROFILE = "enriched"

CATEGORICAL_FEATURES = (
    "regionCodeIndex",
    "categoryCodeIndex",
)

CORE_RAW_NUMERIC_FEATURES = (
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

POPULATION_RAW_FEATURES = (
    "floatingPopulation",
    "residentPopulation",
    "workingPopulation",
)

CORE_NUMERIC_FEATURES = (
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

ENRICHED_NUMERIC_FEATURES = CORE_NUMERIC_FEATURES[:-2] + (
    "floatingPopulationLog1p",
    "residentPopulationLog1p",
    "workingPopulationLog1p",
    "floatingPopulationMissing",
    "residentPopulationMissing",
    "workingPopulationMissing",
) + CORE_NUMERIC_FEATURES[-2:]

TARGETS = {
    "nextQuarterSalesGrowthRate": "regression",
    "nextQuarterStoreCountDeclined": "binary",
    "nextQuarterCloseRate": "regression",
    "fourQuarterStoreRetentionRate": "regression",
    "fourQuarterStoreBaseMaintained": "binary",
}


@dataclass(frozen=True)
class FeatureSchema:
    profile: str
    version: str
    categorical: tuple[str, ...]
    numeric: tuple[str, ...]
    raw_numeric: tuple[str, ...]

    @property
    def feature_names(self) -> tuple[str, ...]:
        return self.categorical + self.numeric

    @property
    def checksum(self) -> str:
        payload = json.dumps(
            {
                "version": self.version,
                "categorical": self.categorical,
                "numeric": self.numeric,
            },
            ensure_ascii=True,
            separators=(",", ":"),
        )
        return sha256(payload.encode("ascii")).hexdigest()


SCHEMAS = {
    CORE_PROFILE: FeatureSchema(
        CORE_PROFILE,
        "core-feature-v1",
        CATEGORICAL_FEATURES,
        CORE_NUMERIC_FEATURES,
        CORE_RAW_NUMERIC_FEATURES,
    ),
    ENRICHED_PROFILE: FeatureSchema(
        ENRICHED_PROFILE,
        "enriched-feature-v1",
        CATEGORICAL_FEATURES,
        ENRICHED_NUMERIC_FEATURES,
        CORE_RAW_NUMERIC_FEATURES + POPULATION_RAW_FEATURES,
    ),
}

# Backwards-compatible exports for the core-v1 artifact contract.
FEATURE_SCHEMA_VERSION = SCHEMAS[CORE_PROFILE].version
FEATURE_NAMES = SCHEMAS[CORE_PROFILE].feature_names
NUMERIC_FEATURES = SCHEMAS[CORE_PROFILE].numeric
RAW_NUMERIC_FEATURES = SCHEMAS[CORE_PROFILE].raw_numeric


def schema_for_profile(profile: str) -> FeatureSchema:
    try:
        return SCHEMAS[profile]
    except KeyError as exception:
        raise ValueError(f"Unsupported feature profile: {profile}") from exception


def schema_for_version(version: str) -> FeatureSchema:
    for schema in SCHEMAS.values():
        if schema.version == version:
            return schema
    raise ValueError(f"Unsupported feature schema version: {version}")


def feature_schema_hash(profile: str = CORE_PROFILE) -> str:
    return schema_for_profile(profile).checksum
