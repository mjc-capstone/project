from __future__ import annotations

from dataclasses import dataclass
from hashlib import sha256
import json
from pathlib import Path
from typing import Any, Mapping

import lightgbm as lgb

from ai_insite_model.features.schema import schema_for_version
from ai_insite_model.features.transformer import CoreFeatureTransformer, TransformerState


@dataclass(frozen=True)
class TargetArtifact:
    task: str
    file: str
    sha256: str
    lowerBound: float
    upperBound: float


@dataclass(frozen=True)
class ModelManifest:
    releaseVersion: str
    datasetVersion: str
    featureVersion: str
    featureSchemaVersion: str
    featureSchemaHash: str
    modelType: str
    eligibleForActivation: bool
    transformer: TransformerState
    targets: dict[str, TargetArtifact]


@dataclass(frozen=True)
class LoadedArtifact:
    manifest: ModelManifest
    transformer: CoreFeatureTransformer
    models: dict[str, lgb.Booster]


def load_artifact(release_path: Path) -> LoadedArtifact:
    manifest_path = release_path / "manifest.json"
    if not manifest_path.is_file():
        raise ValueError(f"Missing model manifest: {manifest_path}")
    raw = json.loads(manifest_path.read_text(encoding="utf-8"))
    require_manifest_fields(raw)
    schema = schema_for_version(str(raw["featureSchemaVersion"]))
    if raw["featureSchemaHash"] != schema.checksum:
        raise ValueError("Feature schema checksum does not match server code")
    if tuple(raw["featureNames"]) != schema.feature_names:
        raise ValueError("Feature order does not match server code")

    targets = {
        name: TargetArtifact(
            task=str(value["task"]),
            file=str(value["file"]),
            sha256=str(value["sha256"]),
            lowerBound=float(value["lowerBound"]),
            upperBound=float(value["upperBound"]),
        )
        for name, value in raw["targets"].items()
    }
    models: dict[str, lgb.Booster] = {}
    for name, target in targets.items():
        path = release_path / target.file
        if not path.is_file():
            raise ValueError(f"Missing model file for {name}: {path}")
        actual = file_sha256(path)
        if actual != target.sha256:
            raise ValueError(f"Model checksum mismatch for {name}")
        models[name] = lgb.Booster(model_file=str(path))

    transformer_state = TransformerState.from_dict(raw["transformer"])
    if transformer_state.featureProfile != schema.profile:
        raise ValueError("Transformer profile does not match feature schema")
    manifest = ModelManifest(
        releaseVersion=str(raw["releaseVersion"]),
        datasetVersion=str(raw["datasetVersion"]),
        featureVersion=str(raw["featureVersion"]),
        featureSchemaVersion=str(raw["featureSchemaVersion"]),
        featureSchemaHash=str(raw["featureSchemaHash"]),
        modelType=str(raw["modelType"]),
        eligibleForActivation=bool(raw["eligibleForActivation"]),
        transformer=transformer_state,
        targets=targets,
    )
    return LoadedArtifact(manifest, CoreFeatureTransformer(transformer_state), models)


def file_sha256(path: Path) -> str:
    digest = sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def require_manifest_fields(raw: Mapping[str, Any]) -> None:
    required = {
        "releaseVersion",
        "datasetVersion",
        "featureVersion",
        "featureSchemaVersion",
        "featureSchemaHash",
        "featureNames",
        "modelType",
        "eligibleForActivation",
        "transformer",
        "targets",
    }
    missing = required.difference(raw)
    if missing:
        raise ValueError(f"Manifest fields are missing: {sorted(missing)}")
