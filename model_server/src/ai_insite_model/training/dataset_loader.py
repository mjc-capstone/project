from __future__ import annotations

from dataclasses import dataclass
import json
from pathlib import Path
from typing import Any, Iterator


SPLITS = ("TRAIN", "VALIDATION", "TEST")


@dataclass(frozen=True)
class DatasetRows:
    by_split: dict[str, list[dict[str, Any]]]

    @property
    def total(self) -> int:
        return sum(len(rows) for rows in self.by_split.values())


def load_ndjson(path: Path) -> DatasetRows:
    by_split = {split: [] for split in SPLITS}
    for line_number, row in rows(path):
        split = str(row.get("split", ""))
        if split not in by_split:
            raise ValueError(f"Unknown split at line {line_number}: {split}")
        if not isinstance(row.get("features"), dict):
            raise ValueError(f"Missing features object at line {line_number}")
        if not isinstance(row.get("labels"), dict):
            raise ValueError(f"Missing labels object at line {line_number}")
        by_split[split].append(row)
    if any(not by_split[split] for split in SPLITS):
        raise ValueError("TRAIN, VALIDATION and TEST must all contain rows")
    return DatasetRows(by_split)


def rows(path: Path) -> Iterator[tuple[int, dict[str, Any]]]:
    with path.open("r", encoding="utf-8") as source:
        for line_number, line in enumerate(source, start=1):
            if not line.strip():
                continue
            try:
                value = json.loads(line)
            except json.JSONDecodeError as exception:
                raise ValueError(
                    f"Invalid NDJSON at line {line_number}"
                ) from exception
            if not isinstance(value, dict):
                raise ValueError(f"NDJSON row must be an object at line {line_number}")
            yield line_number, value
