from ai_insite_model.features.schema import (
    ENRICHED_PROFILE,
    FEATURE_NAMES,
    schema_for_profile,
)
from ai_insite_model.features.transformer import CoreFeatureTransformer


def row(region: str, category: str, sales: int | None) -> dict:
    return {
        "regionCode": region,
        "categoryCode": category,
        "featureAsOfDate": "2026-03-31",
        "features": {
            "salesAmount": sales,
            "salesCount": 10,
            "storeCount": 2,
        },
    }


def test_transformer_has_stable_shape_and_median_imputation() -> None:
    transformer = CoreFeatureTransformer.fit(
        [row("11110515", "CS1", 100), row("11110515", "CS2", 300)]
    )

    transformed = transformer.transform_row(row("11110515", "CS1", None))

    assert len(transformed) == len(FEATURE_NAMES)
    assert transformed[0] == 0
    assert transformed[1] == 0
    assert transformer.missing_rate(row("11110515", "CS1", None)) > 0


def test_unknown_categories_are_marked_out_of_distribution() -> None:
    transformer = CoreFeatureTransformer.fit([row("11110515", "CS1", 100)])

    unknown = row("99999999", "UNKNOWN", 100)

    assert transformer.transform_row(unknown)[:2] == [-1.0, -1.0]
    assert not transformer.has_known_categories(unknown)


def test_enriched_transformer_adds_population_values_and_missing_flags() -> None:
    training = row("11110515", "CS1", 100)
    training["features"].update({
        "floatingPopulation": 1000,
        "residentPopulation": 500,
        "workingPopulation": None,
    })
    transformer = CoreFeatureTransformer.fit([training], ENRICHED_PROFILE)

    transformed = transformer.transform_row(training)
    schema = schema_for_profile(ENRICHED_PROFILE)

    assert len(transformed) == len(schema.feature_names)
    assert transformed[schema.feature_names.index("workingPopulationMissing")] == 1.0
    assert transformer.state.featureProfile == ENRICHED_PROFILE
