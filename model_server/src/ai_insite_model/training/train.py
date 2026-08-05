from __future__ import annotations

import argparse
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import platform
import re
import shutil
from typing import Any

import lightgbm as lgb
import numpy as np
import sklearn

from ai_insite_model.features.schema import (
    CATEGORICAL_FEATURES,
    FEATURE_NAMES,
    FEATURE_SCHEMA_VERSION,
    TARGETS,
    feature_schema_hash,
)
from ai_insite_model.features.transformer import CoreFeatureTransformer
from ai_insite_model.inference.artifact_loader import file_sha256
from ai_insite_model.training.dataset_loader import DatasetRows, load_ndjson
from ai_insite_model.training.evaluate import binary_metrics, regression_metrics


RELEASE_PATTERN = re.compile(r"^[a-z0-9][a-z0-9._-]{2,99}$")


def main() -> None:
    arguments = parse_arguments()
    train_release(
        dataset_path=arguments.dataset,
        artifact_root=arguments.artifact_root,
        release_version=arguments.release,
        dataset_version=arguments.dataset_version,
        feature_version=arguments.feature_version,
        num_threads=arguments.num_threads,
        overwrite=arguments.overwrite,
    )


def train_release(
    dataset_path: Path,
    artifact_root: Path,
    release_version: str,
    dataset_version: str,
    feature_version: str,
    num_threads: int = 4,
    overwrite: bool = False,
) -> Path:
    if not RELEASE_PATTERN.fullmatch(release_version):
        raise ValueError("Release version contains unsupported characters")
    if num_threads < 1 or num_threads > 4:
        raise ValueError("num_threads must be between 1 and 4")

    dataset = load_ndjson(dataset_path)
    transformer = CoreFeatureTransformer.fit(dataset.by_split["TRAIN"])
    matrices = {
        split: transformer.transform_rows(rows)
        for split, rows in dataset.by_split.items()
    }

    artifact_root.mkdir(parents=True, exist_ok=True)
    final_path = artifact_root / release_version
    temporary_path = artifact_root / f".{release_version}.training-{os.getpid()}"
    if temporary_path.exists():
        shutil.rmtree(temporary_path)
    temporary_path.mkdir(parents=True)

    try:
        target_manifests: dict[str, dict[str, Any]] = {}
        target_metrics: dict[str, dict[str, Any]] = {}
        improvements: list[bool] = []
        for index, (target, task) in enumerate(TARGETS.items(), start=1):
            result = train_target(
                target,
                task,
                dataset,
                matrices,
                temporary_path,
                num_threads,
                seed=20260805 + index,
            )
            target_manifests[target] = result["manifest"]
            target_metrics[target] = result["metrics"]
            improvements.append(bool(result["improvedOverBaseline"]))

        metrics = {
            "schemaVersion": "training-metrics-v1",
            "releaseVersion": release_version,
            "datasetVersion": dataset_version,
            "featureVersion": feature_version,
            "rowCounts": {
                split: len(rows) for split, rows in dataset.by_split.items()
            },
            "targets": target_metrics,
            "eligibleForActivation": all(improvements),
        }
        metrics_path = temporary_path / "metrics.json"
        write_json(metrics_path, metrics)

        manifest = {
            "schemaVersion": "model-manifest-v1",
            "releaseVersion": release_version,
            "datasetVersion": dataset_version,
            "featureVersion": feature_version,
            "featureSchemaVersion": FEATURE_SCHEMA_VERSION,
            "featureSchemaHash": feature_schema_hash(),
            "featureNames": list(FEATURE_NAMES),
            "categoricalFeatures": list(CATEGORICAL_FEATURES),
            "modelType": "CPU_LIGHTGBM_CORE_V1",
            "eligibleForActivation": all(improvements),
            "createdAt": datetime.now(timezone.utc).isoformat(),
            "runtime": {
                "python": platform.python_version(),
                "lightgbm": lgb.__version__,
                "scikitLearn": sklearn.__version__,
                "numThreads": num_threads,
            },
            "transformer": transformer.state.to_dict(),
            "targets": target_manifests,
            "metricsFile": {
                "file": metrics_path.name,
                "sha256": file_sha256(metrics_path),
            },
        }
        write_json(temporary_path / "manifest.json", manifest)

        if final_path.exists():
            if not overwrite:
                raise FileExistsError(f"Release already exists: {final_path}")
            shutil.rmtree(final_path)
        temporary_path.replace(final_path)
        return final_path
    except Exception:
        if temporary_path.exists():
            shutil.rmtree(temporary_path)
        raise


def train_target(
    target: str,
    task: str,
    dataset: DatasetRows,
    matrices: dict[str, np.ndarray],
    artifact_path: Path,
    num_threads: int,
    seed: int,
) -> dict[str, Any]:
    prepared: dict[str, tuple[np.ndarray, np.ndarray]] = {}
    for split, rows in dataset.by_split.items():
        indices = [
            index
            for index, row in enumerate(rows)
            if row["labels"].get(target) is not None
        ]
        if not indices:
            raise ValueError(f"No {target} labels in {split}")
        values = np.asarray(
            [float(rows[index]["labels"][target]) for index in indices],
            dtype=np.float64,
        )
        prepared[split] = (matrices[split][indices], values)

    lower, upper = target_bounds(target, prepared["TRAIN"][1])
    if task == "regression":
        prepared = {
            split: (matrix, np.clip(values, lower, upper))
            for split, (matrix, values) in prepared.items()
        }

    train_x, train_y = prepared["TRAIN"]
    validation_x, validation_y = prepared["VALIDATION"]
    test_x, test_y = prepared["TEST"]
    if task == "binary" and len(np.unique(train_y)) != 2:
        raise ValueError(f"Binary target {target} has only one TRAIN class")

    parameters: dict[str, Any] = {
        "objective": "binary" if task == "binary" else "regression_l1",
        "metric": "binary_logloss" if task == "binary" else "l1",
        "learning_rate": 0.05,
        "num_leaves": 31,
        "min_data_in_leaf": 80,
        "feature_fraction": 0.85,
        "bagging_fraction": 0.85,
        "bagging_freq": 1,
        "lambda_l1": 0.1,
        "lambda_l2": 0.5,
        "num_threads": num_threads,
        "seed": seed,
        "feature_fraction_seed": seed,
        "bagging_seed": seed,
        "deterministic": True,
        "force_col_wise": True,
        "verbosity": -1,
    }
    train_set = lgb.Dataset(
        train_x,
        label=train_y,
        feature_name=list(FEATURE_NAMES),
        categorical_feature=list(CATEGORICAL_FEATURES),
        free_raw_data=False,
    )
    validation_set = lgb.Dataset(
        validation_x,
        label=validation_y,
        reference=train_set,
        feature_name=list(FEATURE_NAMES),
        categorical_feature=list(CATEGORICAL_FEATURES),
        free_raw_data=False,
    )
    model = lgb.train(
        parameters,
        train_set,
        num_boost_round=500,
        valid_sets=[validation_set],
        valid_names=["validation"],
        callbacks=[lgb.early_stopping(50, verbose=False), lgb.log_evaluation(0)],
    )
    model_file = f"{target}.txt"
    model_path = artifact_path / model_file
    model.save_model(str(model_path), num_iteration=model.best_iteration)

    validation_prediction = bounded(model.predict(validation_x), lower, upper)
    test_prediction = bounded(model.predict(test_x), lower, upper)
    if task == "binary":
        baseline_value = float(np.mean(train_y))
        metric_function = binary_metrics
        comparison_metric = "brier"
    else:
        baseline_value = float(np.median(train_y))
        metric_function = regression_metrics
        comparison_metric = "mae"
    validation_baseline = np.full_like(validation_y, baseline_value, dtype=np.float64)
    test_baseline = np.full_like(test_y, baseline_value, dtype=np.float64)

    candidate_validation = metric_function(validation_y, validation_prediction)
    candidate_test = metric_function(test_y, test_prediction)
    baseline_validation = metric_function(validation_y, validation_baseline)
    baseline_test = metric_function(test_y, test_baseline)
    improved = candidate_test[comparison_metric] < baseline_test[comparison_metric]

    return {
        "manifest": {
            "task": task,
            "file": model_file,
            "sha256": file_sha256(model_path),
            "lowerBound": lower,
            "upperBound": upper,
            "bestIteration": model.best_iteration,
        },
        "metrics": {
            "availableRows": {
                "TRAIN": len(train_y),
                "VALIDATION": len(validation_y),
                "TEST": len(test_y),
            },
            "baselineValue": baseline_value,
            "baseline": {
                "validation": baseline_validation,
                "test": baseline_test,
            },
            "candidate": {
                "validation": candidate_validation,
                "test": candidate_test,
            },
            "comparisonMetric": comparison_metric,
            "improvedOverBaseline": improved,
        },
        "improvedOverBaseline": improved,
    }


def target_bounds(target: str, train_values: np.ndarray) -> tuple[float, float]:
    if TARGETS[target] == "binary":
        return 0.0, 1.0
    lower = float(np.quantile(train_values, 0.001))
    upper = float(np.quantile(train_values, 0.999))
    if target == "nextQuarterCloseRate":
        lower, upper = max(lower, 0.0), min(upper, 100.0)
    if lower >= upper:
        raise ValueError(f"Target {target} has no usable numeric range")
    return lower, upper


def bounded(values: np.ndarray, lower: float, upper: float) -> np.ndarray:
    result = np.clip(np.asarray(values, dtype=np.float64), lower, upper)
    if not np.all(np.isfinite(result)):
        raise ValueError("Model returned non-finite predictions")
    return result


def write_json(path: Path, value: Any) -> None:
    path.write_text(
        json.dumps(value, ensure_ascii=False, indent=2, allow_nan=False) + "\n",
        encoding="utf-8",
    )


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Train AI Insite core-v1 models")
    parser.add_argument("--dataset", type=Path, required=True)
    parser.add_argument("--artifact-root", type=Path, default=Path("/artifacts"))
    parser.add_argument("--release", required=True)
    parser.add_argument("--dataset-version", required=True)
    parser.add_argument("--feature-version", default="feature-v3-building")
    parser.add_argument("--num-threads", type=int, default=4)
    parser.add_argument("--overwrite", action="store_true")
    return parser.parse_args()


if __name__ == "__main__":
    main()
