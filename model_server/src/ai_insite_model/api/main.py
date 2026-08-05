from __future__ import annotations

from contextlib import asynccontextmanager
import os
from pathlib import Path
from time import perf_counter

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse

from ai_insite_model.api.schemas import (
    ActiveModelResponse,
    HealthResponse,
    PredictionRequest,
    PredictionResponse,
)
from ai_insite_model.inference.predictor import ActiveModel


active_model = ActiveModel()


@asynccontextmanager
async def lifespan(_: FastAPI):
    release = os.getenv("MODEL_RELEASE")
    if release:
        active_model.load(
            Path(os.getenv("MODEL_BASE_PATH", "/models")),
            release,
        )
    yield


app = FastAPI(
    title="AI Insite Model Server",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health/live", response_model=HealthResponse)
def live() -> HealthResponse:
    return HealthResponse(status="UP")


@app.get("/health/ready", response_model=HealthResponse)
def ready() -> HealthResponse | JSONResponse:
    if not active_model.ready():
        return JSONResponse(
            status_code=503,
            content=HealthResponse(status="NOT_READY").model_dump(),
        )
    release = active_model.predictor().artifact.manifest.releaseVersion
    return HealthResponse(status="READY", modelReleaseVersion=release)


@app.get("/v1/models/active", response_model=ActiveModelResponse)
def model_info() -> ActiveModelResponse:
    try:
        manifest = active_model.predictor().artifact.manifest
    except RuntimeError as exception:
        raise HTTPException(status_code=503, detail=str(exception)) from exception
    return ActiveModelResponse(
        releaseVersion=manifest.releaseVersion,
        datasetVersion=manifest.datasetVersion,
        featureVersion=manifest.featureVersion,
        featureSchemaVersion=manifest.featureSchemaVersion,
        modelType=manifest.modelType,
        eligibleForActivation=manifest.eligibleForActivation,
    )


@app.post("/v1/predictions", response_model=PredictionResponse)
def predict(request: PredictionRequest) -> PredictionResponse:
    started = perf_counter()
    try:
        result = active_model.predictor().predict(request)
    except RuntimeError as exception:
        raise HTTPException(status_code=503, detail=str(exception)) from exception
    except ValueError as exception:
        raise HTTPException(status_code=409, detail=str(exception)) from exception
    elapsed = max(0, round((perf_counter() - started) * 1000))
    return PredictionResponse(
        requestId=request.requestId,
        modelReleaseVersion=result.releaseVersion,
        predictions=result.predictions,
        quality=result.quality,
        inferenceMillis=elapsed,
    )
