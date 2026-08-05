from __future__ import annotations

from dataclasses import dataclass
import math
from pathlib import Path
from threading import RLock
from typing import Any

import numpy as np

from ai_insite_model.api.schemas import (
    PredictionQuality,
    PredictionRequest,
    PredictionValues,
)
from ai_insite_model.inference.artifact_loader import LoadedArtifact, load_artifact


@dataclass(frozen=True)
class PredictionResult:
    releaseVersion: str
    predictions: PredictionValues
    quality: PredictionQuality


class CoreModelPredictor:
    def __init__(self, artifact: LoadedArtifact) -> None:
        self.artifact = artifact

    def predict(self, request: PredictionRequest) -> PredictionResult:
        manifest = self.artifact.manifest
        if request.requestedModelReleaseVersion != manifest.releaseVersion:
            raise ValueError("Requested model release is not active")
        if request.featureVersion != manifest.featureVersion:
            raise ValueError("Feature version does not match active model")

        market = request.marketFeatures.model_dump()
        row: dict[str, Any] = {
            "regionCode": market.pop("regionCode"),
            "categoryCode": market.pop("categoryCode"),
            "featureAsOfDate": request.featureAsOfDate,
            "features": market,
        }
        vector = np.asarray(
            [self.artifact.transformer.transform_row(row)],
            dtype=np.float64,
        )
        raw = {
            name: finite_prediction(model.predict(vector)[0], name)
            for name, model in self.artifact.models.items()
        }
        for name, target in manifest.targets.items():
            raw[name] = min(max(raw[name], target.lowerBound), target.upperBound)

        missing_rate = self.artifact.transformer.missing_rate(row)
        known_categories = self.artifact.transformer.has_known_categories(row)
        warnings: list[str] = []
        if not known_categories:
            warnings.append("UNKNOWN_REGION_OR_CATEGORY")
        if missing_rate > 0.3:
            warnings.append("HIGH_CORE_FEATURE_MISSING_RATE")

        values = PredictionValues(
            nextQuarterSalesGrowthRate=raw["nextQuarterSalesGrowthRate"],
            storeDeclineProbability=raw["nextQuarterStoreCountDeclined"],
            nextQuarterCloseRate=raw["nextQuarterCloseRate"],
            fourQuarterStoreRetentionRate=raw["fourQuarterStoreRetentionRate"],
            storeBaseMaintainedProbability=raw[
                "fourQuarterStoreBaseMaintained"
            ],
        )
        quality = PredictionQuality(
            inDistribution=known_categories and missing_rate <= 0.3,
            missingFeatureRate=missing_rate,
            warnings=warnings,
        )
        return PredictionResult(manifest.releaseVersion, values, quality)


class ActiveModel:
    def __init__(self) -> None:
        self._lock = RLock()
        self._predictor: CoreModelPredictor | None = None
        self._error: str | None = None

    def load(self, base_path: Path, release_version: str) -> None:
        try:
            loaded = CoreModelPredictor(load_artifact(base_path / release_version))
        except Exception as exception:
            with self._lock:
                self._error = str(exception)
                self._predictor = None
            raise
        with self._lock:
            self._predictor = loaded
            self._error = None

    def predictor(self) -> CoreModelPredictor:
        with self._lock:
            if self._predictor is None:
                raise RuntimeError(self._error or "No active model is loaded")
            return self._predictor

    def ready(self) -> bool:
        with self._lock:
            return self._predictor is not None


def finite_prediction(value: Any, target: str) -> float:
    parsed = float(value)
    if not math.isfinite(parsed):
        raise ValueError(f"Model returned non-finite prediction for {target}")
    return parsed
