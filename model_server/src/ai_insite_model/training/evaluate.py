from __future__ import annotations

import math

import numpy as np
from sklearn.metrics import roc_auc_score


def regression_metrics(actual: np.ndarray, predicted: np.ndarray) -> dict[str, float]:
    error = predicted - actual
    return {
        "mae": float(np.mean(np.abs(error))),
        "rmse": float(math.sqrt(float(np.mean(np.square(error))))),
    }


def binary_metrics(actual: np.ndarray, probability: np.ndarray) -> dict[str, float]:
    clipped = np.clip(probability, 1e-8, 1 - 1e-8)
    result = {
        "brier": float(np.mean(np.square(clipped - actual))),
        "logLoss": float(
            -np.mean(actual * np.log(clipped) + (1 - actual) * np.log(1 - clipped))
        ),
    }
    result["rocAuc"] = (
        float(roc_auc_score(actual, clipped))
        if len(np.unique(actual)) == 2
        else float("nan")
    )
    return result
