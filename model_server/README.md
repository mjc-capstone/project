# AI Insite Model Server

서울 상권의 행정동-업종-분기 피처로 5개 시장 대리 지표를 예측하는 CPU
LightGBM 학습기와 FastAPI 추론 서버다. Python 의존성은 Spring 프로젝트와
분리하며 학습과 추론은 Docker 이미지로 실행한다.

## 모델 프로필

| 프로필 | 스키마 | 입력 |
|---|---|---|
| `core` | `core-feature-v1` | 매출, 점포, QoQ 변화율, 계산 점수, 분기 |
| `enriched` | `enriched-feature-v1` | core + 유동·상주·직장인구 log1p 및 결측 플래그 |

비용과 건축물 피처는 현재 학습 데이터 결측률이 높아 `enriched-v1`에서 제외한다.
결측률을 개선한 뒤 새 스키마 버전으로 추가한다.

## 테스트

```powershell
docker build -f docker/Dockerfile.trainer `
  -t ai-insite/model-trainer:lightgbm-v1 .

docker run --rm --entrypoint pytest `
  -v "${PWD}\tests:/tests:ro" `
  ai-insite/model-trainer:lightgbm-v1 -q /tests
```

## 학습

```powershell
docker run --rm --cpus=4 --memory=8g `
  -v "${PWD}\data:/data:ro" `
  -v "${PWD}\artifacts:/artifacts" `
  ai-insite/model-trainer:lightgbm-v1 `
  --dataset /data/seoul-commercial-enriched-2023q2-2026q1-v1.ndjson `
  --artifact-root /artifacts `
  --release enriched-v1-2026-08 `
  --dataset-version seoul-commercial-enriched-2023q2-2026q1-v1 `
  --feature-version feature-v4-enriched `
  --feature-profile enriched `
  --num-threads 4
```

아티팩트는 LightGBM native text, `manifest.json`, `metrics.json`으로 구성한다.
manifest에는 피처 순서와 checksum, 데이터셋·피처 버전, 런타임 버전을 기록한다.
모든 대상이 단순 baseline을 이긴 경우에만 `eligibleForActivation=true`가 된다.

## 런타임 이미지

```powershell
docker build -f docker/Dockerfile.runtime `
  --build-arg MODEL_RELEASE=enriched-v1-2026-08 `
  -t ai-insite/model-server:enriched-v1-2026-08 .
```

API:

- `GET /health/live`
- `GET /health/ready`
- `GET /v1/models/active`
- `POST /v1/predictions`

활성 자격이 없는 후보 이미지는 smoke test와 비교 평가에만 사용하고 운영 설정에
연결하지 않는다.
