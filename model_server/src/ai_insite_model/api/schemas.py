from __future__ import annotations

from datetime import date
import math
from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator


FiniteFloat = Annotated[float, Field(allow_inf_nan=False)]
Probability = Annotated[float, Field(ge=0.0, le=1.0, allow_inf_nan=False)]


class StrictModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class MarketFeatures(StrictModel):
    categoryCode: str = Field(min_length=1, max_length=30)
    regionCode: str = Field(min_length=8, max_length=20)
    salesAmount: FiniteFloat | None = None
    salesCount: FiniteFloat | None = None
    salesGrowthRateQoq: FiniteFloat | None = None
    storeCount: FiniteFloat | None = None
    storeGrowthRateQoq: FiniteFloat | None = None
    demandScore: FiniteFloat | None = None
    competitionScore: FiniteFloat | None = None
    marketScore: FiniteFloat | None = None
    stabilityScore: FiniteFloat | None = None
    closureRiskSignal: FiniteFloat | None = None

    @field_validator("salesAmount", "salesCount", "storeCount")
    @classmethod
    def require_non_negative(cls, value: float | None) -> float | None:
        if value is not None and value < 0:
            raise ValueError("count and amount values must be non-negative")
        return value


class UserCondition(StrictModel):
    budget: FiniteFloat | None = Field(default=None, ge=0)
    maxMonthlyRent: FiniteFloat | None = Field(default=None, ge=0)
    targetMonthlySales: FiniteFloat | None = Field(default=None, ge=0)
    preferredAreaSquareMeter: FiniteFloat | None = Field(default=None, gt=0)
    operationType: str | None = Field(default=None, max_length=30)
    franchise: bool | None = None


class PredictionRequest(StrictModel):
    requestId: str = Field(min_length=1, max_length=100)
    schemaVersion: Literal["prediction-input-v1"]
    requestedModelReleaseVersion: str = Field(min_length=1, max_length=100)
    featureVersion: str = Field(min_length=1, max_length=50)
    featureSnapshotId: int | None = Field(default=None, gt=0)
    featureAsOfDate: date
    marketFeatures: MarketFeatures
    userCondition: UserCondition | None = None


class PredictionValues(StrictModel):
    nextQuarterSalesGrowthRate: FiniteFloat
    storeDeclineProbability: Probability
    nextQuarterCloseRate: FiniteFloat
    fourQuarterStoreRetentionRate: FiniteFloat
    storeBaseMaintainedProbability: Probability


class PredictionQuality(StrictModel):
    inDistribution: bool
    missingFeatureRate: Probability
    warnings: list[str]


class PredictionResponse(StrictModel):
    requestId: str
    schemaVersion: Literal["prediction-output-v1"] = "prediction-output-v1"
    modelReleaseVersion: str
    predictions: PredictionValues
    quality: PredictionQuality
    inferenceMillis: int = Field(ge=0)


class HealthResponse(StrictModel):
    status: Literal["UP", "READY", "NOT_READY"]
    modelReleaseVersion: str | None = None


class ActiveModelResponse(StrictModel):
    releaseVersion: str
    datasetVersion: str
    featureVersion: str
    featureSchemaVersion: str
    modelType: str
    eligibleForActivation: bool
