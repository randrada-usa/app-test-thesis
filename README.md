# FruitGrade-Android

An Android proof-of-concept application for grading banana ripeness using on-device TensorFlow Lite inference. Built for thesis research on automated fruit quality assessment.

## Features

- **Solo Grade** — Quick single-image ripeness assessment
- **Batch Grade** — Take 5 photos and get a majority-vote aggregated result
- **Manual Capture** — User-triggered camera shots (no auto-burst)
- **Gallery Picker** — Test with existing photos without using the camera
- **Live Progress** — 0-100% progress bar during inference
- **History** — Persistent scan results with saved image previews
- **Dual Model** — Ships both MobileNetV3-Small and MobileNetV3-Large; user selects at runtime

## Tech Stack

- **Language:** Kotlin + Python
- **UI:** Jetpack Compose (Figma-designed)
- **Camera:** CameraX
- **ML Runtime:** TensorFlow Lite 2.17.0
- **Models:** MobileNetV3-Small / MobileNetV3-Large (converted from TensorFlow 2.19.0)
- **Database:** Room 2.5.2 (SQLite) with KSP
- **Build:** Gradle with Kotlin DSL
- **Architecture:** MVVM (ViewModel + Repository + Room)

## Grade Mapping

| Grade | Label | Display Color |
|-------|-------|---------------|
| G1 | Unripe | Brown |
| G2 | Ripe | Green |
| G3 | Overripe | Yellow |
| G4 | Rotten | Red |

## Project Structure

```
app/src/main/java/com/example/fruitgrade/
├── data/           # Room entities, DAOs, repository
├── ml/             # TFLiteClassifier, ImagePreprocessor, BatchAggregator
├── ui/screens/     # Home, Scan, Result, History screens
├── ui/theme/       # Color, Theme, Type
└── viewmodel/      # ScanViewModel, HistoryViewModel
```

## Requirements

- Android 8.0+ (API 26+)
- Camera hardware
- No internet required (fully offline)

## Related Repository

- **[Algo-Test](https://github.com/randrada-usa/Algo-Test.git)** — Thesis algorithm, model training, and dataset processing

## License

For academic / thesis use.

---

*This is a proof-of-concept application. Real-world accuracy may vary depending on lighting conditions, camera quality, and image similarity to the training dataset.*
