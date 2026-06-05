# FruitGrade-Android — Project Overview & Roadmap

> **Purpose:** Single source of truth for assistant context. Paste this at the start of any new session.
> **Related Repo:** [Algo-Test](https://github.com/randrada-usa/Algo-Test.git) (thesis algorithm + model training)

---

## 1. Project Identity

| Field | Value |
|---|---|
| App Name | FruitGrade (Proof-of-Concept) |
| Platform | Android only (minSdk 26 / Android 8.0) |
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM (ViewModel + Repository + Room) |
| Camera | CameraX |
| Inference | TensorFlow Lite (`.tflite`) |
| Local DB | Room (SQLite) |
| IDE | Google Antigravity (primary editor) |
| Build / Device Testing | Android Studio (SDK, Gradle, ADB) |

---

## 2. What Has Been Done (Current Status)

- [x] **Tech stack decided** — Kotlin + Compose + CameraX + TFLite + Room
- [x] **Architecture planned** — MVVM with Repository pattern
- [x] **Features scoped** — Solo/Batch scan, manual capture, history, model info
- [ ] Android project initialized — NOT YET DONE
- [ ] Gradle dependencies configured — NOT YET DONE
- [ ] TFLite classifier implemented — NOT YET DONE
- [ ] Camera flow built — NOT YET DONE
- [ ] UI screens created — NOT YET DONE
- [ ] Room database setup — NOT YET DONE
- [ ] Physical device testing — NOT YET DONE

---

## 3. Tech Stack & Dependencies

| Layer | Choice | Reason |
|---|---|---|
| **Language** | Kotlin | Native Android, official TFLite support |
| **UI** | Jetpack Compose | Faster dev than XML, modern, theming built-in |
| **Navigation** | Navigation Compose | Type-safe, Compose-native |
| **Camera** | CameraX | Official Google library, simpler than Camera2 |
| **ML Runtime** | TensorFlow Lite | Matches thesis pipeline, runs offline |
| **ML Helpers** | TFLite Support Library | Image preprocessing, label mapping |
| **Database** | Room | Survives reinstalls, standard Android abstraction |
| **Async** | Kotlin Coroutines | Inference must run off main thread |
| **Build** | Gradle (Kotlin DSL) | Standard Android build system |

### Key Dependencies (`libs.versions.toml` or `build.gradle.kts`)

```kotlin
// AndroidX Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.7.7")

// CameraX
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.14.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

## 4. Model Assets

Place these files in `app/src/main/assets/`:

| File | Approx Size | Source |
|---|---|---|
| `small_model.tflite` | ~5 MB | Notebook `05_tflite_conversion.ipynb` in Algo-Test repo |
| `large_model.tflite` | ~12 MB | Notebook `05_tflite_conversion.ipynb` in Algo-Test repo |

**Behavior:** App ships with **both** models. User selects via dropdown at runtime.

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

### 5.3 Timer

- Starts when user taps **"Start Scan"**.
- Ends when final aggregated result is computed and displayed.
- Duration displayed on `ResultScreen` in **ms**.
- Duration also saved in history.

### 5.4 Aggregation Logic (ported from notebook)

```kotlin
// Pseudocode — exact logic in BatchAggregator.kt
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
    
    // Optional: if avg confidence < 0.40, flag as "Uncertain"
}
```

### 5.5 History Detail

Each history entry must show:
- Timestamp
- Model used (Small / Large)
- Scan mode (Solo / Batch)
- **Scan duration in ms**
- **All 5 individual predictions** (batch mode) or 1 (solo mode)
- Final grade
- Final confidence %
- Method used (majority vote / tie broken / low confidence)

### 5.6 Model Info Screen

Displays the 6 thesis metrics for the **selected model**:
| Metric | Description |
|---|---|
| Top-1 Accuracy | Overall test accuracy |
| Macro Precision | Per-class precision average |
| Macro Recall | Per-class recall average |
| Macro F1 | Harmonic mean |
| Inference Latency | 100-run CPU average (ms) |
| Model Size | `.tflite` file size (MB) |

> **Open Question:** Should these metrics be hardcoded in Kotlin or loaded from a JSON file generated by the notebooks?

---

## 6. File Structure (Target)

```
FruitGrade-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   ├── small_model.tflite
│   │   │   │   └── large_model.tflite
│   │   │   ├── java/com/example/fruitgrade/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── FruitGradeApplication.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── ScanResult.kt          (Room Entity)
│   │   │   │   │   ├── ScanDao.kt             (Room DAO)
│   │   │   │   │   ├── AppDatabase.kt         (Room Database)
│   │   │   │   │   └── HistoryRepository.kt
│   │   │   │   ├── ml/
│   │   │   │   │   ├── TFLiteClassifier.kt    (model loading + inference)
│   │   │   │   │   ├── ImagePreprocessor.kt     (bitmap → 224×224 → ByteBuffer)
│   │   │   │   │   └── BatchAggregator.kt     (majority vote logic)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── ScanScreen.kt
│   │   │   │   │   │   ├── ResultScreen.kt
│   │   │   │   │   │   ├── HistoryScreen.kt
│   │   │   │   │   │   └── ModelInfoScreen.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   └── components/
│   │   │   │   │       └── (reusable UI pieces)
│   │   │   │   └── viewmodel/
│   │   │   │       └── ScanViewModel.kt
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── colors.xml
│   │   │       │   └── strings.xml
│   │   │       └── mipmap-xxx/
│   │   │           └── (app icons)
│   │   └── test/
│   │       └── java/... (unit tests)
│   ├── build.gradle.kts
│   └── .gitignore
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── .gitignore
└── README.md
```

---

## 7. Permissions

`AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

> **Note:** `INTERNET` is for Antigravity IDE preview. `CAMERA` is runtime-requested.

---

## 8. Data Model

```kotlin
@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long,

    val modelName: String,        // "MobileNetV3-Small" or "MobileNetV3-Large"

    val scanMode: String,         // "solo" or "batch"

    val durationMs: Long,

    val individualPredictions: String,  // JSON: ["ripe", "ripe", "overripe", ...]

    val individualConfidences: String,  // JSON: [0.92, 0.88, 0.76, ...]

    val finalGrade: String,       // e.g., "ripe"

    val finalConfidence: Float,

    val methodUsed: String        // "majority_vote", "tie_broken", "low_confidence"
)
```

> **Note:** Room doesn't support `List<String>` directly. Serialize lists to JSON strings using `org.json` or Gson.

---

## 9. Development Phases

### Phase 0 — Project Setup (~30 min)
- [ ] Create new Android project in Antigravity (Empty Compose Activity template)
- [ ] Configure `build.gradle.kts` with all dependencies
- [ ] Add permissions to `AndroidManifest.xml`
- [ ] Copy both `.tflite` files to `app/src/main/assets/`
- [ ] Verify build: `./gradlew assembleDebug`

### Phase 1 — Core Infrastructure (~1 hour)
- [ ] `ImagePreprocessor.kt` — Bitmap → 224×224, normalize [0,1]
- [ ] `TFLiteClassifier.kt` — load model from `assets/`, run inference
- [ ] `BatchAggregator.kt` — majority vote, tie-break, low-confidence flag
- [ ] Room setup: `ScanResult`, `ScanDao`, `AppDatabase`
- [ ] `HistoryRepository.kt` — insert and fetch
- [ ] Unit tests: `BatchAggregator` with hardcoded arrays

### Phase 2 — Camera & Scan Flow (~1.5 hours)
- [ ] `HomeScreen` — model dropdown + "Solo Grade" / "Batch Grade"
- [ ] `ScanScreen` — CameraX preview, manual capture, counter, "Start Scan"
- [ ] `ScanViewModel` — timer → capture → preprocess → infer → aggregate → save
- [ ] Runtime camera permission request
- [ ] Thumbnail row at bottom

### Phase 3 — Results & History (~1 hour)
- [ ] `ResultScreen` — grade, confidence, method, duration, all 5 predictions
- [ ] `HistoryScreen` — scrollable list, expandable detail cards
- [ ] Compose Navigation graph
- [ ] Handle empty history, "Uncertain" grades

### Phase 4 — Model Info & Polish (~45 min)
- [ ] `ModelInfoScreen` — 6 metrics display
- [ ] Navigation icon to open Model Info
- [ ] Color-coded grades (green=ripe, yellow=overripe, red=rotten, brown=uncertain)
- [ ] Loading spinner during inference
- [ ] Error states (permission denied, model load fail)
- [ ] Dark mode support (Material3 automatic)

### Phase 5 — Testing & Deployment (~45 min)
- [ ] Unit tests: `BatchAggregator`, `ImagePreprocessor`
- [ ] Install APK on physical phone via ADB
- [ ] Benchmark scan duration
- [ ] Edge cases: rotation, denied permission, kill mid-scan, both models
- [ ] Generate release APK if needed

---

## 10. Known Issues & Fallbacks

| Problem | Solution |
|---|---|
| TFLite model fails to load | Verify `input_shape=(1, 224, 224, 3)` and dtype is `float32` |
| Camera preview freezes | Check CameraX lifecycle binding and permissions |
| Inference too slow on phone | Ensure model runs on CPU thread pool, not main thread |
| Room migration error | Wipe app data or bump version and provide migration |
| Batch accuracy lower than expected | Verify class order matches notebook: `['overripe', 'ripe', 'rotten', 'unripe']` |

---

## 11. Assistant Instructions

When helping with this project:
- **Assume Android-only** — do not suggest iOS, Flutter, or React Native.
- **Use Kotlin + Jetpack Compose** — no XML layouts unless absolutely necessary.
- **Respect the 4-grade mapping:** `unripe=G1, ripe=G2, overripe=G3, rotten=G4`.
- **Keep TFLite compatibility** — avoid custom ops that TFLite doesn't support.
- **Manual capture only** — do not suggest auto-burst or continuous capture.
- **Both models must ship** — user selects Small or Large at runtime.
- **Scan timer** starts at "Start Scan", ends at result display.
- **History must persist** — use Room, not in-memory lists.

---

## 12. Open Questions

- [ ] Where do `ModelInfoScreen` metrics come from? (Hardcoded Kotlin vs. JSON asset file)
- [ ] Color theme preference? (Default Material3 vs. banana ripeness colors)
- [ ] History auto-cleanup? (Keep last N scans vs. keep everything forever)
- [ ] Export history? (Share CSV of scans — future work)

---

## 13. Next Immediate Action

1. Create the `FruitGrade-Android` repo on GitHub.
2. Initialize an Empty Compose Activity project in Google Antigravity.
3. Configure `build.gradle.kts` with dependencies.
4. Copy this `app_overview.md` into the repo root.

Then proceed to **Phase 0** setup.

---

*End of document — paste at the start of any new assistant session.*
