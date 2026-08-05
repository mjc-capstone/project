from __future__ import annotations

from dataclasses import dataclass
from datetime import date
import math
from typing import Any, Iterable, Mapping

import numpy as np

from ai_insite_model.features.schema import (
    CORE_PROFILE,
    ENRICHED_PROFILE,
    POPULATION_RAW_FEATURES,
    schema_for_profile,
)


@dataclass(frozen=True)
class TransformerState:
    regionCodes: tuple[str, ...]
    categoryCodes: tuple[str, ...]
    medians: dict[str, float]
    featureProfile: str = CORE_PROFILE

    def to_dict(self) -> dict[str, Any]:
        return {
            "regionCodes": list(self.regionCodes),
            "categoryCodes": list(self.categoryCodes),
            "medians": self.medians,
            "featureProfile": self.featureProfile,
        }

    @classmethod
    def from_dict(cls, value: Mapping[str, Any]) -> "TransformerState":
        return cls(
            regionCodes=tuple(str(item) for item in value["regionCodes"]),
            categoryCodes=tuple(str(item) for item in value["categoryCodes"]),
            medians={str(key): float(item) for key, item in value["medians"].items()},
            featureProfile=str(value.get("featureProfile", CORE_PROFILE)),
        )


class CoreFeatureTransformer:
    def __init__(self, state: TransformerState) -> None:
        self.state = state
        self.schema = schema_for_profile(state.featureProfile)
        self._regions = {code: index for index, code in enumerate(state.regionCodes)}
        self._categories = {
            code: index for index, code in enumerate(state.categoryCodes)
        }

    @classmethod
    def fit(
        cls,
        rows: Iterable[Mapping[str, Any]],
        feature_profile: str = CORE_PROFILE,
    ) -> "CoreFeatureTransformer":
        materialized = list(rows)
        if not materialized:
            raise ValueError("Transformer training rows must not be empty")
        regions = tuple(sorted({str(row["regionCode"]) for row in materialized}))
        categories = tuple(sorted({str(row["categoryCode"]) for row in materialized}))
        medians: dict[str, float] = {}
        schema = schema_for_profile(feature_profile)
        for name in schema.raw_numeric:
            values = [
                number(row.get("features", {}).get(name))
                for row in materialized
            ]
            finite = [value for value in values if value is not None]
            medians[name] = float(np.median(finite)) if finite else 0.0
        return cls(TransformerState(regions, categories, medians, feature_profile))

    def transform_rows(self, rows: Iterable[Mapping[str, Any]]) -> np.ndarray:
        return np.asarray([self.transform_row(row) for row in rows], dtype=np.float64)

    def transform_row(self, row: Mapping[str, Any]) -> list[float]:
        raw = row.get("features", row.get("marketFeatures", {}))
        as_of = parse_date(row.get("featureAsOfDate") or raw.get("featureAsOfDate"))
        sales_amount = self._value(raw, "salesAmount")
        sales_count = self._value(raw, "salesCount")
        values = [
            float(self._regions.get(str(row.get("regionCode")), -1)),
            float(self._categories.get(str(row.get("categoryCode")), -1)),
            math.log1p(max(sales_amount, 0.0)),
            math.log1p(max(sales_count, 0.0)),
            self._value(raw, "salesGrowthRateQoq"),
            self._value(raw, "storeCount"),
            self._value(raw, "storeGrowthRateQoq"),
            self._value(raw, "demandScore"),
            self._value(raw, "competitionScore"),
            self._value(raw, "marketScore"),
            self._value(raw, "stabilityScore"),
            self._value(raw, "closureRiskSignal"),
        ]
        if self.state.featureProfile == ENRICHED_PROFILE:
            populations = [number(raw.get(name)) for name in POPULATION_RAW_FEATURES]
            values.extend(
                math.log1p(max(self._value(raw, name), 0.0))
                for name in POPULATION_RAW_FEATURES
            )
            values.extend(1.0 if value is None else 0.0 for value in populations)
        values.extend((
            float(((as_of.month - 1) // 3) + 1),
            float(as_of.year * 4 + ((as_of.month - 1) // 3)),
        ))
        if len(values) != len(self.schema.feature_names):
            raise AssertionError("Feature vector does not match schema")
        return values

    def missing_rate(self, row: Mapping[str, Any]) -> float:
        raw = row.get("features", row.get("marketFeatures", {}))
        missing = sum(
            number(raw.get(name)) is None for name in self.schema.raw_numeric
        )
        return missing / len(self.schema.raw_numeric)

    def has_known_categories(self, row: Mapping[str, Any]) -> bool:
        return (
            str(row.get("regionCode")) in self._regions
            and str(row.get("categoryCode")) in self._categories
        )

    def _value(self, raw: Mapping[str, Any], name: str) -> float:
        value = number(raw.get(name))
        return self.state.medians[name] if value is None else value


def number(value: Any) -> float | None:
    if value is None or isinstance(value, bool):
        return None
    try:
        parsed = float(value)
    except (TypeError, ValueError):
        return None
    return parsed if math.isfinite(parsed) else None


def parse_date(value: Any) -> date:
    if isinstance(value, date):
        return value
    if not value:
        raise ValueError("featureAsOfDate is required")
    return date.fromisoformat(str(value))
