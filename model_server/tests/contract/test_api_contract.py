import json
from pathlib import Path

import pytest
from fastapi.testclient import TestClient
from pydantic import ValidationError

from ai_insite_model.api.main import app
from ai_insite_model.api.schemas import PredictionRequest


FIXTURE = Path(__file__).parents[1] / "fixtures" / "prediction_request.json"


def test_prediction_request_golden_json_is_valid() -> None:
    request = PredictionRequest.model_validate_json(FIXTURE.read_text(encoding="utf-8"))

    assert request.schemaVersion == "prediction-input-v1"
    assert request.marketFeatures.categoryCode == "CS100001"


def test_prediction_request_rejects_unknown_and_non_finite_values() -> None:
    value = json.loads(FIXTURE.read_text(encoding="utf-8"))
    value["unexpected"] = True
    with pytest.raises(ValidationError):
        PredictionRequest.model_validate(value)

    del value["unexpected"]
    value["marketFeatures"]["marketScore"] = float("nan")
    with pytest.raises(ValidationError):
        PredictionRequest.model_validate(value)


def test_health_is_live_but_not_ready_without_an_artifact() -> None:
    with TestClient(app) as client:
        assert client.get("/health/live").json() == {
            "status": "UP",
            "modelReleaseVersion": None,
        }
        response = client.get("/health/ready")
        assert response.status_code == 503
        assert response.json()["status"] == "NOT_READY"
