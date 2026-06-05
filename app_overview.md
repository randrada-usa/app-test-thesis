# FruitGrade-Android — Project Overview & Roadmap

> **Purpose:** Single source of truth for assistant context. Paste this at the start of any new session.
> **Related Repo:** [Algo-Test](https://github.com/randrada-usa/Algo-Test.git) (thesis algorithm + model training)

---

## 1. Project Identity

| Field | Value |
|---|---|
| App Name | FruitGrade (Proof-of-Concept) |
| Platform | Android only (minSdk 26 / Android 8.0) |
| Language | Kotlin, Python |
| UI Framework | Jetpack Compose |
| UI Design | Figma |
| Architecture | MVVM (ViewModel + Repository + Room) |
| Camera | CameraX |
| Inference | TensorFlow Lite (`.tflite`) |
| Local DB | Room (SQLite) |
| IDE | Android Studio / Google Antigravity |
| Build / Device Testing | Gradle (Kotlin DSL) |

---

## 2. Current Status — All Phases Complete

- [x] **Tech stack decided** — Kotlin + Compose + CameraX + TFLite + Room
- [x] **Architecture planned** — MVVM with Repository pattern
- [x] **Features scoped** — Solo/Batch scan, manual capture, history, gallery picker
- [x] **Android project initialized** — Empty Compose Activity template
- [x] **Gradle dependencies configured** — All dependencies in `libs.versions.toml`
- [x] **TFLite classifier implemented** — `TFLiteClassifier.kt` with 2.17.0 runtime
- [x] **Camera flow built** — CameraX preview with manual capture, permission handling
- [x] **UI screens created** — Home, Scan, Result, History screens
- [x] **Room database setup** — `ScanResult`, `ScanDao`, `AppDatabase` with migration
- [x] **Physical device testing** — Tested on Realme 5 Pro
- [x] **Gallery picker** — System photo picker for testing without camera
- [x] **Progress indicator** — 0-100% linear progress bar during inference
- [x] **Image previews in history** — Saved scan images displayed in history cards
- [x] **Preprocessing fixed** — [0, 255] float32 range (matches training)

---

## 3. Tech Stack & Dependencies

| Component | Tool/Technology |
|-----------|----------------|
| **Mobile Platform** | Android (API 26+) |
| **Programming Language** | Kotlin, Python |
| **UI Framework** | Jetpack Compose |
| **UI Design** | Figma |
| **Camera** | CameraX |
| **Dataset Processing** | Python (image_dataset_from_directory) |
| **Model Training** | TensorFlow / Keras (MobileNetV3 backbone) |
| **Mobile AI Runtime** | TensorFlow Lite 2.17.0 |
| **ML Model** | MobileNetV3-Small / MobileNetV3-Large |
| **Local Database** | Room 2.5.2 (SQLite) |
| **Image Preprocessing** | Bitmap → 224×224, [0, 255] Float32 |
| **Async** | Kotlin Coroutines (Dispatchers.Default for inference) |
| **Build** | Gradle (Kotlin DSL) with KSP |
| **Navigation** | Navigation Compose |

---

## 4. Model Assets

| File | Size | Source |
|---|---|---|
| `small_model.tflite` | ~1.1 MB | Notebook `05_tflite_conversion.ipynb` in Algo-Test repo |
| `large_model.tflite` | ~3.3 MB | Notebook `05_tflite_conversion.ipynb` in Algo-Test repo |

**Behavior:** App ships with **both** models. User selects via dropdown at runtime.
**Preprocessing:** Model expects `[0, 255] float32` inputs (NOT normalized to `[0, 1]`).

---

## 5. Feature Specifications

### 5.1 Scan Modes

| Mode | Images | Use Case |
|---|---|---|
| **Solo Grade** | 1 | Quick single-fruit check |
| **Batch Grade** | 5 | Thesis requirement — majority vote across bunch |

### 5.2 Capture Behavior

- **Manual tap only.** No auto-burst.
- Solo mode: 1 tap → immediate inference.
- Batch mode: up to 5 taps, counter shows "Image 2/5".
- Thumbnails of captured images shown in a row at bottom of screen.
- **Gallery picker** available for testing without physical camera.

### 5.3 Timer (Duration)

- **Measures inference time only** — preprocessing + model execution.
- **Does NOT include** camera setup, photo capture, or user interaction time.
- Duration displayed on `ResultScreen` in **ms**.
- Duration saved in history.

### 5.4 Aggregation Logic

```kotlin
// Exact logic in BatchAggregator.kt
function gradeBatch(predictions: List<Pair<label, confidence>>) {
    val votes = predictions.map { it.label }
    val majority = majorityVote(votes)
    
    if (majority.size == 1) {
        return (majority[0], avgConfidence(majority[0]), "majority_vote")
    } else {
        // Tie: break by average confidence
        val winner = majority.maxBy { avgConfidence(it) }
        return (winner, avgConfidence(winner), "tie_broken_by_confidence")
    }
    
    // If avg confidence < 0.40, flag as "low_confidence"
}
```

### 5.5 History Detail

Each history entry shows:
- Timestamp
- Model used (Small / Large)
- Scan mode (Solo / Batch)
- **Scan duration in ms** (inference time only)
- **All 5 individual predictions** (batch mode) or 1 (solo mode)
- **Captured images** (thumbnails in history, expanded view shows all)
- Final grade
- Final confidence %
- Method used (majority vote / tie broken / low confidence)

### 5.6 UX Features

- **Progress bar** — 0-100% linear indicator during inference
- **Shutter flash** — White screen flash on photo capture
- **Button locking** — Capture/Gallery buttons disabled during processing
- **Color-coded grades** — Green (G2), Yellow (G3), Red (G4), Brown (G1)
- **Dark mode** — Material3 automatic theming
- **Error handling** — Camera error with retry button

---

## 6. Grade Mapping

| Grade | Display | Color |
|-------|---------|-------|
| `unripe` | Grade 1 — Unripe | Brown |
| `ripe` | Grade 2 — Ripe | Green |
| `overripe` | Grade 3 — Overripe | Yellow |
| `rotten` | Grade 4 — Rotten | Red |

**Model output order:** `['overripe', 'ripe', 'rotten', 'unripe']` (index 0→3)

---

## 7. File Structure

```
FruitGrade-Android/
├── app/src/main/
│   ├── assets/
│   │   ├── small_model.tflite
│   │   └── large_model.tflite
│   ├── java/com/example/fruitgrade/
│   │   ├── MainActivity.kt
│   │   ├── FruitGradeApplication.kt
│   │   ├── data/
│   │   │   ├── ScanResult.kt          (Room Entity + image paths)
│   │   │   ├── ScanDao.kt
│   │   │   ├── AppDatabase.kt         (Migration 1→2)
│   │   │   └── HistoryRepository.kt
│   │   ├── ml/
│   │   │   ├── TFLiteClassifier.kt    (model loading + inference)
│   │   │   ├── ImagePreprocessor.kt     ([0,255] float32 preprocessing)
│   │   │   └── BatchAggregator.kt     (majority vote logic)
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── ScanScreen.kt      (pre-scan + camera + progress)
│   │   │   │   ├── ResultScreen.kt
│   │   │   │   └── HistoryScreen.kt   (with image previews)
│   │   │   └── theme/
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt
│   │   │       └── Type.kt
│   │   └── viewmodel/
│   │       ├── ScanViewModel.kt
│   │       └── HistoryViewModel.kt
│   └── res/... (icons, themes)
├── app/src/test/
│   └── java/... (BatchAggregator unit tests)
├── gradle/libs.versions.toml
├── TECH_STACK.md
└── app_overview.md
```

---

## 8. Permissions

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

---

## 9. Data Model

```kotlin
@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val modelName: String,
    val scanMode: String,
    val durationMs: Long,
    val individualPredictions: String,  // JSON: ["ripe", "ripe", "overripe", ...]
    val individualConfidences: String,    // JSON: [0.92, 0.88, 0.76, ...]
    val finalGrade: String,
    val finalConfidence: Float,
    val methodUsed: String,
    val previewImagePaths: String = "[]"  // JSON: ["/path/to/img1.jpg", ...]
)
```

---

## 10. Known Limitations

| Limitation | Details |
|---|---|
| **Accuracy** | Model accuracy drops on non-dataset images (lighting, angle, background differences) |
| **16 KB page size** | TFLite 2.17.0 native libs not aligned — warning for Android 15+ devices |
| **Dataset-specific** | Best performance on images similar to training data |
| **PoC scope** | Designed for thesis demonstration, not production deployment |

---

## 11. Assistant Instructions

When helping with this project:
- **Android-only** — Kotlin + Jetpack Compose
- **4-grade mapping:** `unripe=G1, ripe=G2, overripe=G3, rotten=G4`
- **Manual capture only** — no auto-burst
- **Both models ship** — user selects at runtime
- **Duration measures inference time** — not camera setup
- **Preprocessing:** `[0, 255]` float32 — never normalize to `[0, 1]`
- **History persists** — Room database with image previews

---

*End of document — updated to reflect current implementation.*
